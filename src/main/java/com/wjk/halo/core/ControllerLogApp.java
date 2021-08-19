package com.wjk.halo.core;

import cn.hutool.core.io.resource.Resource;
import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wjk.halo.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 该类使用切面的方法，在请求发送到controller之间，打印请求日志，在处理好请求之后，返回给前端之前，打印返回日志
 */

@Aspect
@Component
@Slf4j
public class ControllerLogApp {

    @Pointcut("execution(*  *..*.*.controller..*.*(..))")
    public void controller() { }

    /**
     * Proceedingjoinpoint 继承了 JoinPoint。是在JoinPoint的基础上暴露出 proceed 这个方法，用来手动执行目标方法
     * joinPoint.getTarget()：返回目标对象
     * getSimpleName()：获得类的简写名称，即只有类名
     * joinPoint.getSignature()：获取目标方法的详细信息（修饰符 + 包名 + 组件名(类名) + 方法的名字）
     * joinPoint.getArgs()： 获取带参方法的参数
     */
    @Around("controller()")
    public Object controller(ProceedingJoinPoint joinPoint) throws Throwable{
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        /**
         * RequestContextHolder：用来存储请求信息一个容器
         */
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(requestAttributes).getRequest();
        /**
         * 打印请求日志，包括请求信息，目标类，目标方法名称，目标方法参数
         */
        printRequestLog(request, className, methodName, args);
        long start = System.currentTimeMillis();
        /**
         * 执行目标方法。因此该行之前相当于前置通知，改行之后相当于后置通知
         */
        Object returnObj = joinPoint.proceed();
        /**
         * 打印返回日志，包括请求信息，目标类，目标方法名称，返回体，整个处理时间
         */
        printResponseLog(request, className, methodName, returnObj, System.currentTimeMillis() - start);
        return returnObj;
    }

    /**
     * 将请求信息保存到日志
     * @param request：该次请求
     * @param clazzName：目标类名称
     * @param methodName：目标方法名称
     * @param args：目标方法参数
     * @throws JsonProcessingException
     */
    private void printRequestLog(HttpServletRequest request, String clazzName, String methodName, Object[] args) throws JsonProcessingException{
        log.debug("Request URL: [{}], URI: [{}], Request Method: [{}], IP: [{}]",
                request.getRequestURL(),
                request.getRequestURI(),
                request.getMethod(),
                ServletUtil.getClientIP(request));
        if (args == null || !log.isDebugEnabled()){
            return;
        }

        boolean shouldNotLog = false;
        for (Object arg : args){
            if (arg == null ||
                    arg instanceof HttpServletRequest ||
                    arg instanceof HttpServletResponse ||
                    arg instanceof MultipartFile ||
                    arg.getClass().isAssignableFrom(MultipartFile[].class)) {
                shouldNotLog = true;
                break;
            }
        }
        if (!shouldNotLog){
            String requestBody = JsonUtils.objectToJson(args);
            log.debug("{}.{} Parameters: [{}]", clazzName, methodName, requestBody);
        }
    }

    /**
     * 将返回信息保存到日志
     * @param request
     * @param className
     * @param methodName
     * @param returnObj
     * @param usage
     * @throws JsonProcessingException
     */
    private void printResponseLog(HttpServletRequest request, String className, String methodName, Object returnObj, long usage) throws JsonProcessingException{
        if (log.isDebugEnabled()){
            String returnData = "";

            if (returnObj != null){
                if (returnObj instanceof ResponseEntity){
                    ResponseEntity responseEntity = (ResponseEntity) returnObj;
                    if (responseEntity.getBody() instanceof Resource){
                        returnData = "[ BINARY DATA ]";
                    }else {
                        returnData = toString(responseEntity.getBody());
                    }
                }else {
                    returnData = toString(returnObj);
                }
            }
            log.debug("{}.{} Response: [{}], usage: [{}]ms", className, methodName, returnData, usage);
        }
    }

    @NonNull
    private String toString(@NonNull Object obj) throws JsonProcessingException{
        String toString = "";

        if (obj.getClass().isAssignableFrom(byte[].class) && obj instanceof Resource){
            toString = "[ BINARY DATA ]";
        }else {
            toString = JsonUtils.objectToJson(obj);
        }
        return toString;
    }

}
