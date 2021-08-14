package com.wjk.halo.aspect;

import com.wjk.halo.model.entity.BaseComment;
import com.wjk.halo.security.context.SecurityContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SensitiveConcealAspect {

    /**
     * 切入点为SensitiveConceal注解
     */
    @Pointcut("@annotation(com.wjk.halo.annotation.SensitiveConceal)")
    public void pointcut(){}

    /**
     * 用于将评论中的敏感信息置空：email,ipAddress
     * @param comment
     * @return
     */
    private Object sensitiveMask(Object comment){
        if (comment instanceof BaseComment){
            ((BaseComment) comment).setEmail("");
            ((BaseComment) comment).setIpAddress("");
        }
        return comment;
    }

    /**
     *作用于SensitiveConceal注解的环绕通知
     *
     *ProceedingJoinPoint对象是JoinPoint的子接口,该对象只用在@Around的切面方法中,
     * 添加了
     * Object proceed() throws Throwable //执行目标方法
     * Object proceed(Object[] var1) throws Throwable //传入的新的参数去执行目标方法
     *
     * 该注解一般用在数据库查询上，执行数据库查询，返回查询结果。对返回的查询结果进行处理，防止泄漏敏感的信息
     */
    @Around("pointcut()")
    public Object mask(ProceedingJoinPoint joinPoint) throws Throwable{

        //运行目标方法
        Object result = joinPoint.proceed();

        //如果已经授权，则返回全部结果
        if (SecurityContextHolder.getContext().isAuthenticated()){
            return result;
        }

        //如果没有授权，则将敏感信息置空
        if (result instanceof Iterable){
            ((Iterable<?>) result).forEach(this::sensitiveMask);
        }

        return sensitiveMask(result);

    }

}
