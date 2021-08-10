package com.wjk.halo.factory;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    @Override
    @NonNull
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return null;
    }

    private static class StringToEnumConverter<T extends Enum<T>> implements Converter<String, T>{
        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            return Enum.valueOf(this.enumType, source.toUpperCase());
        }
    }

}
