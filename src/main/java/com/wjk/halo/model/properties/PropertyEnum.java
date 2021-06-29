package com.wjk.halo.model.properties;

import com.sun.istack.internal.NotNull;
import com.wjk.halo.model.enums.ValueEnum;

public interface PropertyEnum extends ValueEnum<String> {

    static <T> T convertTo(@NotNull String value, @NotNull Class<T> type){
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
}
