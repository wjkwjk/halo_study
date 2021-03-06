package com.wjk.halo.utils;

import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ServletUtils {

    private ServletUtils(){}

    @NonNull
    public static Optional<HttpServletRequest> getCurrentRequest(){
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(requestAttributes -> requestAttributes instanceof ServletRequestAttributes)
                .map(requestAttributes -> (ServletRequestAttributes) requestAttributes)
                .map(ServletRequestAttributes::getRequest);
    }

    @Nullable
    public static String getRequestIp(){
        return getCurrentRequest().map(ServletUtil::getClientIP).orElse(null);
    }

    @Nullable
    public static String getHeaderIgnoreCase(String header){
        return getCurrentRequest().map(request -> ServletUtil.getHeaderIgnoreCase(request, header)).orElse(null);
    }

}
