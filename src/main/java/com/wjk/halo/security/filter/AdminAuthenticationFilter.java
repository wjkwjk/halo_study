package com.wjk.halo.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.exception.AuthenticationException;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.security.authentication.AuthenticationImpl;
import com.wjk.halo.security.context.SecurityContext;
import com.wjk.halo.security.context.SecurityContextHolder;
import com.wjk.halo.security.context.SecurityContextImpl;
import com.wjk.halo.security.handler.DefaultAuthenticationFailureHandler;
import com.wjk.halo.security.service.OneTimeTokenService;
import com.wjk.halo.security.support.UserDetail;
import com.wjk.halo.security.util.SecurityUtils;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.wjk.halo.model.support.HaloConst.ADMIN_TOKEN_HEADER_NAME;
import static com.wjk.halo.model.support.HaloConst.ADMIN_TOKEN_QUERY_NAME;

@Slf4j
@Component
@Order(1)
public class AdminAuthenticationFilter extends AbstractAuthenticationFilter{

    private final HaloProperties haloProperties;
    private final UserService userService;

    public AdminAuthenticationFilter(AbstractStringCacheStore cacheStore,
                                     UserService userService,
                                     HaloProperties haloProperties,
                                     OptionService optionService,
                                     OneTimeTokenService oneTimeTokenService,
                                     ObjectMapper objectMapper){
        super(haloProperties, optionService, cacheStore, oneTimeTokenService);
        this.userService = userService;
        this.haloProperties = haloProperties;

        //在访问接口前需要进行验证的api
        addUrlPatterns("/api/admin/**", "/api/content/comments");

        //在访问接口前不需要进行验证的api
        addExcludeUrlPatterns(
                "/api/admin/login",
                "/api/admin/refresh/*",
                "/api/admin/installations",
                "/api/admin/migrations/halo",
                "/api/admin/is_installed",
                "/api/admin/password/code",
                "/api/admin/password/reset",
                "/api/admin/login/precheck"
        );

        DefaultAuthenticationFailureHandler failureHandler = new DefaultAuthenticationFailureHandler();
        failureHandler.setProductionEnv(haloProperties.isProductionEnv());
        failureHandler.setObjectMapper(objectMapper);

        setFailureHandler(failureHandler);
    }

    @Override
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!haloProperties.isAuthEnabled()){
            //如果不允许授权，则查询当前用户，并用当前用户的身份授权，将当前用户的身份存储在本地线程
            userService.getCurrentUser().ifPresent(user ->
                    SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(new UserDetail(user)))));
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);

        //根据request是否有带有token，判断是否登陆过
        if (StringUtils.isBlank(token)){
            throw new AuthenticationException("未登录，请登录后访问");
        }
        //根据缓存中是否带有token，判断token是否有效
        Optional<Integer> optionalUserId = cacheStore.getAny(SecurityUtils.buildTokenAccessKey(token), Integer.class);

        if (!optionalUserId.isPresent()){
            throw new AuthenticationException("Token 已过期或不存在").setErrorData(token);
        }
        //从数据库根据id查找用户
        User user = userService.getById(optionalUserId.get());

        UserDetail userDetail = new UserDetail(user);

        //将用户信息存储在线程本地
        SecurityContextHolder.setContext(new SecurityContextImpl(new AuthenticationImpl(userDetail)));

        filterChain.doFilter(request, response);

    }

    @Override
    protected String getTokenFromRequest(HttpServletRequest request) {
        return getTokenFromRequest(request, ADMIN_TOKEN_QUERY_NAME, ADMIN_TOKEN_HEADER_NAME);
    }
}
