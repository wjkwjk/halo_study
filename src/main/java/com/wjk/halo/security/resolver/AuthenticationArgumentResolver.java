package com.wjk.halo.security.resolver;

import com.wjk.halo.exception.AuthenticationException;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.security.authentication.Authentication;
import com.wjk.halo.security.context.SecurityContextHolder;
import com.wjk.halo.security.support.UserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

@Slf4j
public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

    public AuthenticationArgumentResolver() {
        log.debug("Initializing AuthenticationArgumentResolver");
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return Authentication.class.isAssignableFrom(parameterType)
                || UserDetail.class.isAssignableFrom(parameterType)
                || User.class.isAssignableFrom(parameterType);
    }

    @Override
    @Nullable
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        log.debug("Handle AuthenticationArgument");

        Class<?> parameterType = parameter.getParameterType();

        Authentication authentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(() -> new AuthenticationException("You haven't signed in yet"));

        if (Authentication.class.isAssignableFrom(parameterType)){
            return authentication;
        }else if (UserDetail.class.isAssignableFrom(parameterType)){
            return authentication.getDetail();
        }else if (User.class.isAssignableFrom(parameterType)){
            return authentication.getDetail().getUser();
        }

        throw new UnsupportedOperationException("Unknown parameter type: " + parameterType);

    }
}
