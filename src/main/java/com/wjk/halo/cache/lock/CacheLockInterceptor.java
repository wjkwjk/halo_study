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

    @Around("@annotation(com.wjk.halo.cache.lock.CacheLock)")
    public Object interceptCacheLock(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        log.debug("Starting locking: [{}]", methodSignature.toString());

        CacheLock cacheLock = methodSignature.getMethod().getAnnotation(CacheLock.class);

        String cacheLockKey = buildCacheLockKey(cacheLock, joinPoint);

        log.debug("Built lock key: [{}]", cacheLockKey);

        try {
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

    private String buildCacheLockKey(@NonNull CacheLock cacheLock, @NonNull ProceedingJoinPoint joinPoint){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        StringBuilder cacheKeyBuilder = new StringBuilder(CACHE_LOCK_PREFOX);

        String delimiter = cacheLock.delimiter();

        if (StringUtils.isNotBlank(cacheLock.prefix())){
            cacheKeyBuilder.append(cacheLock.prefix());
        }else {
            cacheKeyBuilder.append(methodSignature.getMethod().toString());
        }

        Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();

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
