package com.wjk.halo.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.wjk.halo.model.support.HaloConst.ADMIN_TOKEN_HEADER_NAME;
import static com.wjk.halo.model.support.HaloConst.API_ACCESS_KEY_HEADER_NAME;

/**
 * 解决跨域问题
 *      跨域只存在于浏览器端，不存在于安卓/ios/Node.js/python/ java等其它环境
 *      跨域请求能发出去，服务端能收到请求并正常返回结果，只是结果被浏览器拦截了。
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CorsFilter extends GenericFilterBean {

    // "Content-Type,ADMIN-Authorization,API-Authorization"
    private final static String ALLOW_HEADERS = StringUtils.joinWith(",", HttpHeaders.CONTENT_TYPE, ADMIN_TOKEN_HEADER_NAME, API_ACCESS_KEY_HEADER_NAME);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        //设置响应头，允许跨域访问

        /**
         *  表示允许 origin 发起跨域请求。
         */
        String originHeaderValue = httpServletRequest.getHeader(HttpHeaders.ORIGIN);
        if (StringUtils.isNotBlank(originHeaderValue)){
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, originHeaderValue);
        }

        /**
         * 支持自定义头
         */
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOW_HEADERS);

        /**
         * GET,POST,OPTIONS，PUT,DELETE 表示允许跨域请求的方法
         */
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");

        /**
         * 允许cookie
         */
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

        /**
         * 表示在3600秒内不需要再发送预校验请求
         */
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");

        //转交给下一个过滤器处理
        if (!CorsUtils.isPreFlightRequest(httpServletRequest)){
            chain.doFilter(httpServletRequest, httpServletResponse);
        }

    }
}
