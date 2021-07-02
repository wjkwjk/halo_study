package com.wjk.halo.utils;

import cn.hutool.core.bean.BeanException;
import com.wjk.halo.exception.BeanUtilsException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

public class BeanUtils {
    private BeanUtils(){}

    public static void updateProperties(@NonNull Object source, @NonNull Object target){
        try {
            org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
        }catch (BeansException e){
            throw new BeanUtilsException("Failed to copy properties", e);
        }
    }

    @NonNull
    private static String[] getNullPropertyNames(@NonNull Object source){
        return getNullPropertyNameSet(source).toArray(new String[0]);
    }

    @NonNull
    private static Set<String> getNullPropertyNameSet(@NonNull Object source){
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors){
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);

            if (propertyValue == null){
                emptyNames.add(propertyName);
            }
        }
        return emptyNames;
    }

    @Nullable
    public static <T> T transformFrom(@Nullable Object source, @NonNull Class<T> targetClass){
        if (source==null){
            return null;
        }
        try {
            T targetInstance = targetClass.newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, targetInstance, getNullPropertyNames(source));
            return targetInstance;
        }catch (Exception e){
            throw new BeanUtilsException("Failed to new " + targetClass.getName() + " instance or copy properties", e);
        }
    }

}
