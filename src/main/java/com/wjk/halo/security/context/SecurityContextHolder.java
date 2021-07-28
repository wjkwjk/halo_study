package com.wjk.halo.security.context;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class SecurityContextHolder {

    //线程内部存储类
    private final static ThreadLocal<SecurityContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private SecurityContextHolder(){}

    @NonNull
    public static SecurityContext getContext(){
        SecurityContext context = CONTEXT_HOLDER.get();
        if (context==null){
            context = createEmptyContent();
            CONTEXT_HOLDER.set(context);
        }
        return context;
    }

    public static void setContext(@Nullable SecurityContext context){
        CONTEXT_HOLDER.set(context);
    }

    @NonNull
    private static SecurityContext createEmptyContent(){
        return new SecurityContextImpl(null);
    }

    public static void clearContext(){
        CONTEXT_HOLDER.remove();
    }

}
