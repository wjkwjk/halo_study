package com.wjk.halo.aspect;

import com.wjk.halo.annotation.DisableOnCondition;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.exception.ForbiddenException;
import com.wjk.halo.model.enums.Mode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class DisableOnConditionAspect {

    private final HaloProperties haloProperties;

    public DisableOnConditionAspect(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    /**
     * 切入点为自定义的DisableOnCondition注解
     */
    @Pointcut("@annotation(com.wjk.halo.annotation.DisableOnCondition)")
    public void pointcut(){}

    /**
     * 可以通过设置api上的DisableOnCondition中的mode，来禁止某些api
     * 环绕通知
     *
     */
    @Around("pointcut() && @annotation(disableApi)")
    public Object around(ProceedingJoinPoint joinPoint, DisableOnCondition disableApi) throws Throwable{
        Mode mode = disableApi.mode();
        if (haloProperties.getMode().equals(mode)){
            throw new ForbiddenException("禁止访问");
        }

        return joinPoint.proceed();
    }
}
