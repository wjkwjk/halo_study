package com.wjk.halo.filter;

import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * OncePerRequestFilter 确保一次请求只通过一次filter，而不重复执行
 * 日志过滤器，用于记录请求的ip等信息
 */

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 9)
public class LogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String remoteAddr = ServletUtil.getClientIP(request);

        log.debug("");
        log.debug("Starting url: [{}], method: [{}], ip: [{}]", request.getRequestURL(), request.getMethod(), remoteAddr);

        // Set start time
        long startTime = System.currentTimeMillis();

        // Do filter
        filterChain.doFilter(request, response);

        log.debug("Ending   url: [{}], method: [{}], ip: [{}], status: [{}], usage: [{}] ms", request.getRequestURL(), request.getMethod(), remoteAddr, response.getStatus(), System.currentTimeMillis() - startTime);
        log.debug("");
    }
}
