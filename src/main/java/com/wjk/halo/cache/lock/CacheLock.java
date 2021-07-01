package com.wjk.halo.cache.lock;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheLock {

    @AliasFor("value")
    String prefix() default "";

    @AliasFor("prefix")
    String value() default "";

    long expired() default 5;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    String delimiter() default ":";

    boolean autoDelete() default true;

    boolean traceRequest() default false;

}
