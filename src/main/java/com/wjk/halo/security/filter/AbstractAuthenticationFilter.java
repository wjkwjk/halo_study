package com.wjk.halo.security.filter;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.exception.AbstractHaloException;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.exception.ForbiddenException;
import com.wjk.halo.exception.NotInstallException;
import com.wjk.halo.model.enums.Mode;
import com.wjk.halo.model.properties.PrimaryProperties;
import com.wjk.halo.security.context.SecurityContextHolder;
import com.wjk.halo.security.handler.AuthenticationFailureHandler;
import com.wjk.halo.security.handler.DefaultAuthenticationFailureHandler;
import com.wjk.halo.security.service.OneTimeTokenService;
import com.wjk.halo.service.OptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.wjk.halo.model.support.HaloConst.ONE_TIME_TOKEN_HEADER_NAME;
import static com.wjk.halo.model.support.HaloConst.ONE_TIME_TOKEN_QUERY_NAME;

@Slf4j
public abstract class AbstractAuthenticationFilter extends OncePerRequestFilter {

    protected final AntPathMatcher antPathMatcher;
    protected final HaloProperties haloProperties;
    protected final OptionService optionService;
    protected final AbstractStringCacheStore cacheStore;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    private final OneTimeTokenService oneTimeTokenService;
    private volatile AuthenticationFailureHandler failureHandler;

    private Set<String> excludeUrlPatterns = new HashSet<>(16);
    private Set<String> urlPatterns = new LinkedHashSet<>();

    AbstractAuthenticationFilter(HaloProperties haloProperties,
                                 OptionService optionService,
                                 AbstractStringCacheStore cacheStore,
                                 OneTimeTokenService oneTimeTokenService){
        this.haloProperties = haloProperties;
        this.optionService = optionService;
        this.cacheStore = cacheStore;
        this.oneTimeTokenService = oneTimeTokenService;

        antPathMatcher = new AntPathMatcher();
    }

    public void addUrlPatterns(String... urlPatterns){
        Collections.addAll(this.urlPatterns, urlPatterns);
    }

    public void addExcludeUrlPatterns(@NonNull String... excludeUrlPatterns){
        Collections.addAll(this.excludeUrlPatterns, excludeUrlPatterns);
    }

    public synchronized void setFailureHandler(@NonNull AuthenticationFailureHandler failureHandler){
        this.failureHandler = failureHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);
        //未安装
        if (!isInstalled && !Mode.TEST.equals(haloProperties.getMode())){
            //将错误写入response
            getFailureHandler().onFailure(request, response, new NotInstallException("当前博客还没有初始化"));
            return;
        }

        try {
            //进行一次性token验证，包括是否存储有该token，该token对应的请求来源地址与当前的请求来源地址是否匹配
            //该一次性token验证用于用户登录成功后，存储在线程内部，使得之后的每次的请求不用到内存中读取token
            //主要是为了加快过滤速度
            if (isSufficientOneTimeToken(request)){
                //token验证成功，则继续其他验证
                filterChain.doFilter(request, response);
                return;
            }
            //没有token，则根据是普通用户还是管理员进行授权，即为用户生成权限，并存储在本本地
            //此时需要进行登陆判断
            doAuthenticate(request, response, filterChain);
        }catch (AbstractHaloException e){
            getFailureHandler().onFailure(request, response, e);
        }finally {
            //由于一次性token用于在用户登陆后，防止每次请求到来都需要从内存中取token，导致速度变慢
            //删除一次性过滤器
            SecurityContextHolder.clearContext();
        }

    }

    @NonNull
    private AuthenticationFailureHandler getFailureHandler(){
        if (failureHandler == null){
            synchronized (this){
                if (failureHandler == null){
                    DefaultAuthenticationFailureHandler failureHandler = new DefaultAuthenticationFailureHandler();
                    failureHandler.setProductionEnv(haloProperties.isProductionEnv());
                    this.failureHandler = failureHandler;
                }
            }
        }
        return failureHandler;
    }

    private boolean isSufficientOneTimeToken(HttpServletRequest request){
        //获得token
        final String oneTimeToken = getTokenFromRequest(request, ONE_TIME_TOKEN_QUERY_NAME, ONE_TIME_TOKEN_HEADER_NAME);
        if (StringUtils.isBlank(oneTimeToken)){
            //没有token，返回
            return false;
        }
        //若存在token，则从缓存中获取该token，如果获取不到，则返回失败
        //若缓存中确实存在该token，则获取token对应的值，即该token所对应的请求来源地址
        String allowedUri = oneTimeTokenService.get(oneTimeToken)
                .orElseThrow(() -> new BadRequestException("The one-time token does not exist").setErrorData(oneTimeToken));
        String requestUri = request.getRequestURI();
        //token对应的请求来源地址与当前的请求来源地址不匹配
        if (!StringUtils.equals(requestUri, allowedUri)){
            throw new ForbiddenException("The one-time token does not correspond the request uri").setErrorData(oneTimeToken);
        }
        //缓存中有，token，则返回true，并且删除该token(由于是一次性的)
        oneTimeTokenService.revoke(oneTimeToken);

        return true;
    }

    //请求中，有可能在两个地方存储了token
    //获得token
    protected String getTokenFromRequest(@NonNull HttpServletRequest request, @NonNull String tokenQueryName, @NonNull String tokenHeaderName){
        String accessKey = request.getHeader(tokenHeaderName);

        if (StringUtils.isBlank(accessKey)){
            accessKey = request.getParameter(tokenQueryName);
            log.debug("Got access key from parameter: [{}: {}]", tokenQueryName, accessKey);
        }else {
            log.debug("Got access key from header: [{}: {}]", tokenHeaderName, accessKey);
        }
        return accessKey;
    }

    protected abstract void doAuthenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException;

    @Nullable
    protected abstract String getTokenFromRequest(@NonNull HttpServletRequest httpServletRequest);

    //判断当前访问的api是否需要该过滤器进行过滤
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean result = excludeUrlPatterns.stream().anyMatch(p -> antPathMatcher.match(p, urlPathHelper.getRequestUri(request)));

        return result || urlPatterns.stream().noneMatch(p -> antPathMatcher.match(p, urlPathHelper.getRequestUri(request)));
    }

    @NonNull
    public Set<String> getExcludeUrlPatterns(){
        return excludeUrlPatterns;
    }

    public void setExcludeUrlPatterns(@NonNull Collection<String> excludeUrlPatterns){
        this.excludeUrlPatterns = new HashSet<>(excludeUrlPatterns);
    }

    public Collection<String> getUrlPatterns(){
        return this.urlPatterns;
    }

    public void setUrlPatterns(Collection<String> urlPatterns){
        this.urlPatterns = new LinkedHashSet<>(urlPatterns);
    }

}
