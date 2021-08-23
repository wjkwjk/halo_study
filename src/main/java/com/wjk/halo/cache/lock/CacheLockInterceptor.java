package com.wjk.halo.cache.lock;

import cn.hutool.extra.servlet.ServletUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.exception.FrequentAccessException;
import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;

/**
 * 缓存锁拦截器
 * 是为了防止多个线程同时访问同一个方法。在缓存中添加要被访问的方法的名称以及参数，当有其他线程也要访问该方法时，会首先查找缓存中是否有该方法对应的
 * 键，若已有键，则抛出异常。
 */

@Slf4j
@Aspect
@Configuration
public class CacheLockInterceptor {

    private final static String CACHE_LOCK_PREFOX = "cache_lock_";

    private final static String CACHE_LOCK_VALUE = "locked";

    private final AbstractStringCacheStore cacheStore;

    public CacheLockInterceptor(AbstractStringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    /**
     * CacheLock注解的环绕通知
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.wjk.halo.cache.lock.CacheLock)")
    public Object interceptCacheLock(ProceedingJoinPoint joinPoint) throws Throwable{
        /**
         * 获得目标类
         */
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        log.debug("Starting locking: [{}]", methodSignature.toString());

        /**
         * 获取CacheLock注解
         */
        CacheLock cacheLock = methodSignature.getMethod().getAnnotation(CacheLock.class);

        /**
         * 创建缓存锁的键
         * "cache_lock_" + 目标方法名 + "/" + 参数1 + "/" + 参数2
         *      其中的参数都是用 cacheParam注解 修饰的
         */
        String cacheLockKey = buildCacheLockKey(cacheLock, joinPoint);

        log.debug("Built lock key: [{}]", cacheLockKey);

        try {
            /**
             * 保存到缓存中
             *  键 ： 创建的缓存锁的键
             *  值 ： CacheWrapper，内容为 locked
             */
            Boolean cacheResult = cacheStore.putIfAbsent(cacheLockKey,CACHE_LOCK_VALUE, cacheLock.expired(), cacheLock.timeUnit());

            if (cacheResult == null){
                throw new ServiceException("Unknown reason of cache " + cacheLockKey).setErrorData(cacheLockKey);
            }

            if (!cacheResult) {
                throw new FrequentAccessException("访问过于频繁，请稍后再试！").setErrorData(cacheLockKey);
            }

            // Proceed the method
            return joinPoint.proceed();
        }finally {
            if (cacheLock.autoDelete()){
                cacheStore.delete(cacheLockKey);
                log.debug("Deleted the cache lock: [{}]", cacheLock);
            }
        }

    }

    /**
     * 用于创建保存缓存锁的键
     * 返回    "cache_lock_" + 目标方法名 + "/" + 参数1 + "/" + 参数2
     * 其中的参数都是用 cacheParam注解 修饰的
     * @param cacheLock
     * @param joinPoint
     * @return
     */

    private String buildCacheLockKey(@NonNull CacheLock cacheLock, @NonNull ProceedingJoinPoint joinPoint){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // cache_lock_
        StringBuilder cacheKeyBuilder = new StringBuilder(CACHE_LOCK_PREFOX);

        // ：
        String delimiter = cacheLock.delimiter();

        // "cache_lock_" + 目标方法名
        if (StringUtils.isNotBlank(cacheLock.prefix())){
            cacheKeyBuilder.append(cacheLock.prefix());
        }else {
            cacheKeyBuilder.append(methodSignature.getMethod().toString());
        }

        //获得目标方法每个参数的每个注解
        Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();

        /**
         * 遍历每个参数，将用 CacheParam注解 修饰的参数添加到cacheKeyBuilder
         * 最后cacheKeyBuilder的结果应该为 ： "cache_lock_" + 目标方法名 + "/" + 参数1 + "/" + 参数2
         *  其中的参数都是用 cacheParam注解 修饰的
         */
        for (int i=0; i<parameterAnnotations.length; i++){
            log.debug("Parameter annotation[{}] = {}", i, parameterAnnotations[i]);

            for (int j=0; j<parameterAnnotations[i].length; j++){
                Annotation annotation = parameterAnnotations[i][j];
                log.debug("Parameter annotation[{}][{}]: {}", i, j, annotation);
                if (annotation instanceof CacheParam){
                    Object arg = joinPoint.getArgs()[i];
                    log.debug("Cache param args: [{}]", arg);

                    cacheKeyBuilder.append(delimiter).append(arg.toString());
                }
            }
        }

        if (cacheLock.traceRequest()){
            cacheKeyBuilder.append(delimiter).append(ServletUtils.getRequestIp());
        }
        return cacheKeyBuilder.toString();

    }

}
