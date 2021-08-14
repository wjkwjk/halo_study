package com.wjk.halo.annotation;

import java.lang.annotation.*;

/**
 * 定义注解，但是没有实现该注解的作用，
 * 具体的作用是通过切面实现的，在aspect/SensitiveConcealAspect实现
 * 该注解一般用在数据库查询上，执行数据库查询，返回查询结果。对返回的查询结果进行处理，防止泄漏敏感的信息
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SensitiveConceal {
}
