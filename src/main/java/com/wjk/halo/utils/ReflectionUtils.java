package com.wjk.halo.utils;

import com.sun.istack.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    @Nullable
    public static ParameterizedType getParameterizedType(@NonNull Class<?> superType, Type... genericTypes){
        ParameterizedType currentType = null;

        for (Type genericType : genericTypes){
            if (genericType instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                if (parameterizedType.getRawType().getTypeName().equals(superType.getTypeName())){
                    currentType = parameterizedType;
                    break;
                }
            }
        }
        return currentType;
    }

    @Nullable
    public static ParameterizedType getParameterizedType(@NonNull Class<?> interfaceType, Class<?> implementationClass){
        if (implementationClass == null){
            return null;
        }

        ParameterizedType currentType = getParameterizedType(interfaceType, implementationClass.getGenericInterfaces());

        if (currentType != null){
            return currentType;
        }

        Class<?> superclass = implementationClass.getSuperclass();

        return getParameterizedType(interfaceType, superclass);

    }

    @Nullable
    public static ParameterizedType getParameterizedTypeBySuperClass(@NonNull Class<?> superClassType, Class<?> extensionClass){
        if (extensionClass == null){
            return null;
        }
        return getParameterizedType(superClassType, extensionClass.getGenericSuperclass());
    }


}
