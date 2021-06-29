package com.wjk.halo.utils;

import com.sun.istack.NotNull;

import java.lang.reflect.Method;
import java.util.Locale;

public class ReflectionUtils {
    public static Object getFieldValue(@NotNull String fieldName, @NotNull Object object){
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
