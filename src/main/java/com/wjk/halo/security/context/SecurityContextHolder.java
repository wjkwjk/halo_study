package com.wjk.halo.security.context;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 线程内部存储类，主要用来将token存储在线程内部，使得不用每次请求去内存中取token，加快验证速度
 */
public class SecurityContextHolder {

    //线程内部存储类，可以使得每个线程内部存储数据，别的线程无法访问
    //ThreadLocal相当于内部维护一个map，键值为当前的线程，值为SecurityContext
    private final static ThreadLocal<SecurityContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private SecurityContextHolder(){}

    @NonNull
    public static SecurityContext getContext(){
        //获得当前线程的SecurityContext
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
