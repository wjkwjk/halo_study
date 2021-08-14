package com.wjk.halo.annotation;

import com.wjk.halo.model.enums.Mode;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 定义注解，但是没有实现该注解的作用，
 * 具体的作用是通过切面实现的，在aspect/DisableOnConditionAspect实现
 * 该接口用来controller接口上，通过设置注解的mode/value属性，可以控制哪些接口在哪些模式下使用
 */

/**
 * 该注解可以限制某些条件下禁止访问api
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisableOnCondition {

    @AliasFor("mode")
    Mode value() default Mode.DEMO;

    @AliasFor("value")
    Mode mode() default Mode.DEMO;

}
