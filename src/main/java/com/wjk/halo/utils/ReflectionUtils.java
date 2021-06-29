package com.wjk.halo.utils;

import com.sun.istack.NotNull;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Locale;

public class ReflectionUtils {
    public static Object getFieldValue(@NonNull String fieldName, @NonNull Object object){
        Object value = null;
        try {
            String firstLetter = fieldName.substring(0,1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = object.getClass().getMethod(getter);
            value = method.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
