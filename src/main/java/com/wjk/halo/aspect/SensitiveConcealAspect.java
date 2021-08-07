package com.wjk.halo.aspect;

import com.wjk.halo.model.entity.BaseComment;
import com.wjk.halo.security.context.SecurityContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SensitiveConcealAspect {

    @Pointcut("@annotation(com.wjk.halo.annotation.SensitiveConceal)")
    public void pointcut(){}

    private Object sensitiveMask(Object comment){
        if (comment instanceof BaseComment){
            ((BaseComment) comment).setEmail("");
            ((BaseComment) comment).setIpAddress("");
        }
        return comment;
    }

    public Object mask(ProceedingJoinPoint joinPoint) throws Throwable{
        Object result = joinPoint.proceed();

        if (SecurityContextHolder.getContext().isAuthenticated()){
            return result;
        }

        if (result instanceof Iterable){
            ((Iterable<?>) result).forEach(this::sensitiveMask);
        }

        return sensitiveMask(result);

    }

}
