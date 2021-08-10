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

/**
 * 1. @RequestBody注解，可以把请求里的参数，自动映射到方法里的参数
 * 2. 如果方法里的参数，不只是请求里的参数，还有cookie里的数据，消息头的数据
 * 3. 并且希望Controller的接口仍然接受一个参数对象，参数已经组装好了
 * 4. 这时候需要HandlerMethodArgumentResolver接口
 * 目的是将发过来的请求参数组装好，在使用controller接口的参数进行接收
 */

@Slf4j
public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

    public AuthenticationArgumentResolver() {
        log.debug("Initializing AuthenticationArgumentResolver");
    }

    //条件，当返回值为true时，才调用resolveArgument()方法
    //A.isAssignableFrom(B)，判断A是不是B的父类或者接口或者二者相同
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //判断传进来的参数是不是Authentication、UserDetail、User的子类，如果是，再进行解析
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

        //进行权限验证
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
