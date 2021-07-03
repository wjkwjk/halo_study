package com.wjk.halo.model.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.internal.NotNull;
import com.wjk.halo.model.enums.ValueEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

public interface PropertyEnum extends ValueEnum<String> {

    /**
     *目的是为了String类型的value值转变为指定的类型
     * @param value 数据库全部查询结果中，每一行中列名为value的值
     * @param type  要查询的枚举类型中存储的value类型
     * @param <T>
     * @return
     */
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

    /**
     *
     * @param value
     * @param propertyEnum
     * @return
     */

    /**
     *
     * @param value 数据库全部查询结果中，每一行中列名为value的值
     * @param propertyEnum  需要查找的枚举类型
     * @return
     */
    static Object convertTo(@Nullable String value, @NonNull PropertyEnum propertyEnum){
        if (StringUtils.isBlank(value)){
            value = propertyEnum.defaultValue();
        }
        try {   //判断枚举中的value的类型是否属于枚举类型
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

    /**
     *
     * @param value
     * @param type
     * @param <T>
     * @return
     */
    //返回美剧类型type
    @Nullable
    static <T extends Enum<T>> T convertToEnum(@NonNull String value, @NonNull Class<T> type){
        try {
            return Enum.valueOf(type, value.toUpperCase());
        }catch (Exception e){
            return null;
        }
    }

    /**
     *
     * @return
     */
    //以枚举中的value作为键，以枚举本身作为值
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

    static boolean isSupportedType(Class<?> type){
        if (type==null){
            return false;
        }
        return type.isAssignableFrom(String.class)
                || type.isAssignableFrom(Number.class)
                || type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(Boolean.class)
                || type.isAssignableFrom(Short.class)
                || type.isAssignableFrom(Byte.class)
                || type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(Float.class)
                || type.isAssignableFrom(Enum.class)
                || type.isAssignableFrom(ValueEnum.class);
    }

    Class<?> getType();

    @Nullable
    String defaultValue();

}
