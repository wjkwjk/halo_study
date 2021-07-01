package com.wjk.halo.utils;


import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class ValidationUtils {
    private static volatile Validator VALIDATOR;

    private ValidationUtils() {
    }

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

    public static void validate(Object obj, Class<?>... groups){
        Validator validator = getValidator();

        if (obj instanceof Iterable){
            validate((Iterable<?>) obj, groups);
        }else {
            Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(obj, groups);

            if (!CollectionUtils.isEmpty(constraintViolationSet)){
                throw new ConstraintViolationException(constraintViolationSet);
            }

        }
    }


}
