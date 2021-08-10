package com.wjk.halo.security.handler;

import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjk.halo.exception.AbstractHaloException;
import com.wjk.halo.model.support.BaseResponse;
import com.wjk.halo.utils.ExceptionUtils;
import com.wjk.halo.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class DefaultAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private boolean productionEnv = true;

    private ObjectMapper objectMapper = JsonUtils.DEFAULT_JSON_MAPPER;

    public DefaultAuthenticationFailureHandler() {
    }

    public void setProductionEnv(boolean productionEnv){
        this.productionEnv = productionEnv;
    }

    public void setObjectMapper(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    //用来设置出现异常时，response中的参数信息
    @Override
    public void onFailure(HttpServletRequest request, HttpServletResponse response, AbstractHaloException exception) throws IOException, ServletException {
        log.warn("Handle unsuccessful authentication, ip: [{}]", ServletUtil.getClientIP(request));
        log.error("Authentication failure: [{}], status: [{}], data: [{}]", exception.getMessage(), exception.getStatus(), exception.getErrorData());

        BaseResponse<Object> errorDetail = new BaseResponse<>();

        errorDetail.setStatus(exception.getStatus().value());
        errorDetail.setMessage(exception.getMessage());
        errorDetail.setData(exception.getErrorData());

        if (!productionEnv){
            errorDetail.setDevMessage(ExceptionUtils.getStackTrace(exception));
        }
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(exception.getStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(errorDetail));

    }
}
