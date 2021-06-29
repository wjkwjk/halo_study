package com.wjk.halo.model.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.internal.NotNull;
import com.wjk.halo.model.enums.ValueEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

public interface PropertyEnum extends ValueEnum<String> {

    //将value转为type类型
    static <T> T convertTo(@NonNull String value, @NonNull Class<T> type){
        if (type.isAssignableFrom(String.class)) {
            return (T) value;
        }

        if (type.isAssignableFrom(Integer.class)) {
            return (T) Integer.valueOf(value);
        }

        if (type.isAssignableFrom(Long.class)) {
            return (T) Long.valueOf(value);
        }

        if (type.isAssignableFrom(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        }

        if (type.isAssignableFrom(Short.class)) {
            return (T) Short.valueOf(value);
        }

        if (type.isAssignableFrom(Byte.class)) {
            return (T) Byte.valueOf(value);
        }

        if (type.isAssignableFrom(Double.class)) {
            return (T) Double.valueOf(value);
        }

        if (type.isAssignableFrom(Float.class)) {
            return (T) Float.valueOf(value);
        }

        throw new UnsupportedOperationException("Unsupported convention for blog property type:" + type.getName() + " provided");
    }

    static Object convertTo(@Nullable String value, @NonNull PropertyEnum propertyEnum){
        if (StringUtils.isBlank(value)){
            value = propertyEnum.defaultValue();
        }
        try {
            if (propertyEnum.getType().isAssignableFrom(Enum.class)){
                Class<Enum> type = (Class<Enum>) propertyEnum.getType();
                Enum result = convertToEnum(value, type);
                return result != null ? result : value;
            }
            return convertTo(value, propertyEnum.getType());
        }catch (Exception e){
            return value;
        }

    }

    @Nullable
    static <T extends Enum<T>> T convertToEnum(@NonNull String value, @NonNull Class<T> type){
        try {
            return Enum.valueOf(type, value.toUpperCase());
        }catch (Exception e){
            return null;
        }
    }

    static Map<String, PropertyEnum> getValuePropertyEnumMap(){
        List<Class<? extends PropertyEnum>> propertyEnumClasses = new LinkedList<>();

        propertyEnumClasses.add(PrimaryProperties.class);


        Map<String, PropertyEnum> result = new HashMap<>();

        propertyEnumClasses.forEach(propertyEnumClass -> {
            PropertyEnum[] propertyEnums = propertyEnumClass.getEnumConstants();

            for (PropertyEnum propertyEnum : propertyEnums) {
                result.put(propertyEnum.getValue(), propertyEnum);
            }
        });

        return result;
    }

    Class<?> getType();

    @Nullable
    String defaultValue();

}
