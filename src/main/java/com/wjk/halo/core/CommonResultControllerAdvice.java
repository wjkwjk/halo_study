package com.wjk.halo.core;

import com.wjk.halo.model.support.BaseResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice("com.wjk.halo.controller")
public class CommonResultControllerAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    @NonNull
    public Object beforeBodyWrite(@Nullable Object body,
                                  @NotNull MethodParameter returnType,
                                  @NotNull MediaType contentType,
                                  @NotNull Class<? extends HttpMessageConverter<?>> converterType,
                                  @NotNull ServerHttpRequest request,
                                  @NotNull ServerHttpResponse response) {
        MappingJacksonValue container = getOrCreateContainer(body);

        beforeBodyWriteInternal(container, contentType, returnType, request, response);
        return container;
    }

    private MappingJacksonValue getOrCreateContainer(Object body){
        return body instanceof MappingJacksonValue ? (MappingJacksonValue) body : new MappingJacksonValue(body);
    }

    private void beforeBodyWriteInternal(MappingJacksonValue bodyContainer,
                                         MediaType contentType,
                                         MethodParameter returnType,
                                         ServerHttpRequest request,
                                         ServerHttpResponse response){
        Object returnBody = bodyContainer.getValue();
        if (returnBody instanceof BaseResponse){
            BaseResponse<?> baseResponse = (BaseResponse<?>) returnBody;
            response.setStatusCode(HttpStatus.resolve(baseResponse.getStatus()));
            return;
        }

        BaseResponse<?> baseResponse = BaseResponse.ok(returnBody);
        bodyContainer.setValue(baseResponse);
        response.setStatusCode(HttpStatus.valueOf(baseResponse.getStatus()));

    }

}
