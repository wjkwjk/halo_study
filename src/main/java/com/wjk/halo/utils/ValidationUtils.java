package com.wjk.halo.utils;


import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;

import javax.validation.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ValidationUtils {
    private static volatile Validator VALIDATOR;

    private ValidationUtils() {
    }

    //双锁同步，单例模式，用来生成唯一的验证器
    @NonNull
    public static Validator getValidator(){
        if (VALIDATOR == null){
            synchronized (ValidationUtils.class){
                if (VALIDATOR == null){
                    VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
                }
            }
        }
        return VALIDATOR;
    }

    //obj就是要检查的安装参数
    public static void validate(Object obj, Class<?>... groups){

        //使用单例模式生成唯一验证器
        Validator validator = getValidator();

        //Iterable是集合的顶级接口，Collection继承于Iterable接口
        if (obj instanceof Iterable){
            //验证Iterable对象
            validate((Iterable<?>) obj, groups);
        }else {
            //验证非Iterable对象
            Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(obj, groups);

            if (!CollectionUtils.isEmpty(constraintViolationSet)){
                throw new ConstraintViolationException(constraintViolationSet);
            }

        }
    }

    public static void validate(@Nullable Iterable<?> objs, @Nullable Class<?>... groups) {
        if (objs == null) {
            return;
        }

        // get validator
        Validator validator = getValidator();

        // wrap index
        AtomicInteger i = new AtomicInteger(0);
        final Set<ConstraintViolation<?>> allViolations = new LinkedHashSet<>();
        objs.forEach(obj -> {
            int index = i.getAndIncrement();
            Set<? extends ConstraintViolation<?>> violations = validator.validate(obj, groups);
            violations.forEach(violation -> {
                Path path = violation.getPropertyPath();
                if (path instanceof PathImpl) {
                    PathImpl pathImpl = (PathImpl) path;
                    pathImpl.makeLeafNodeIterableAndSetIndex(index);
                }
                allViolations.add(violation);
            });
        });
        if (!CollectionUtils.isEmpty(allViolations)) {
            throw new ConstraintViolationException(allViolations);
        }
    }

    @NonNull
    public static Map<String, String> mapWithValidError(Set<ConstraintViolation<?>> constraintViolations){
        if (CollectionUtils.isEmpty(constraintViolations)){
            return Collections.emptyMap();
        }
        Map<String, String> errMap = new HashMap<>(4);
        constraintViolations.forEach(constraintViolation ->
                errMap.put(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()));
        return errMap;
    }

    public static Map<String, String> mapWithFieldError(@Nullable List<FieldError> fieldErrors){
        if (CollectionUtils.isEmpty(fieldErrors)){
            return Collections.emptyMap();
        }

        Map<String, String> errMap = new HashMap<>(4);
        fieldErrors.forEach(fieldError -> errMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return errMap;
    }

}
