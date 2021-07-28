package com.wjk.halo.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.exception.AuthenticationException;
import com.wjk.halo.exception.ForbiddenException;
import com.wjk.halo.model.properties.ApiProperties;
import com.wjk.halo.model.properties.CommentProperties;
import com.wjk.halo.security.handler.DefaultAuthenticationFailureHandler;
import com.wjk.halo.security.service.OneTimeTokenService;
import com.wjk.halo.service.OptionService;
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

import static com.wjk.halo.model.support.HaloConst.API_ACCESS_KEY_HEADER_NAME;
import static com.wjk.halo.model.support.HaloConst.API_ACCESS_KEY_QUERY_NAME;

@Slf4j
@Component
@Order(0)
public class ApiAuthenticationFilter extends AbstractAuthenticationFilter {

    private final OptionService optionService;

    ApiAuthenticationFilter(HaloProperties haloProperties,
                            OptionService optionService,
                            AbstractStringCacheStore cacheStore,
                            OneTimeTokenService oneTimeTokenService,
                            ObjectMapper objectMapper) {
        super(haloProperties, optionService, cacheStore, oneTimeTokenService);
        this.optionService = optionService;

        addUrlPatterns("/api/content/**");

        addExcludeUrlPatterns(
                "/api/content/**/comments",
                "/api/content/**/comments/**",
                "/api/content/options/comment"
        );

        DefaultAuthenticationFailureHandler failureHandler = new DefaultAuthenticationFailureHandler();
        failureHandler.setProductionEnv(haloProperties.isProductionEnv());
        failureHandler.setObjectMapper(objectMapper);
        setFailureHandler(failureHandler);

    }

    @Override
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!haloProperties.isAuthEnabled()){
            filterChain.doFilter(request, response);
            return;
        }

        Boolean apiEnabled = optionService.getByPropertyOrDefault(ApiProperties.API_ENABLED, Boolean.class, false);

        if (!apiEnabled){
            throw new ForbiddenException("API has been disabled by blogger currently");
        }

        String accessKey = getTokenFromRequest(request);

        if (StringUtils.isBlank(accessKey)){
            throw new AuthenticationException("Missing API access key");
        }

        Optional<String> optionalAccessKey = optionService.getByProperty(ApiProperties.API_ACCESS_KEY, String.class);

        if (!optionalAccessKey.isPresent()){
            throw new AuthenticationException("API access key hasn't been set by blogger");
        }

        if (!StringUtils.equals(accessKey, optionalAccessKey.get())){
            throw new AuthenticationException("API access key is mismatch").setErrorData(accessKey);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected String getTokenFromRequest(HttpServletRequest request) {
        return getTokenFromRequest(request, API_ACCESS_KEY_QUERY_NAME, API_ACCESS_KEY_HEADER_NAME);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean result = super.shouldNotFilter(request);

        if (antPathMatcher.match("/api/content/*/comments", request.getServletPath())){
            Boolean commentApiEnabled = optionService.getByPropertyOrDefault(CommentProperties.API_ENABLED, Boolean.class, true);
            if (!commentApiEnabled){
                result = false;
            }
        }
        return result;
    }
}
