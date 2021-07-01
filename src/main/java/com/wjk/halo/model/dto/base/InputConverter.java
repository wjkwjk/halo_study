package com.wjk.halo.model.dto.base;

import com.wjk.halo.utils.BeanUtils;
import com.wjk.halo.utils.ReflectionUtils;
import jdk.internal.util.xml.impl.Input;
import org.springframework.lang.Nullable;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public interface InputConverter <DOMAIN>{
    default DOMAIN convertTo(){
        ParameterizedType currentType = parameterizedType();

        Objects.requireNonNull(currentType, "Cannot fetch actual type because parameterized type is null");

        Class<DOMAIN> domainClass = (Class<DOMAIN>) currentType.getActualTypeArguments()[0];

        return BeanUtils.transformFrom(this, domainClass);
    }

    @Nullable
    default ParameterizedType parameterizedType(){
        return ReflectionUtils.getParameterizedType(InputConverter.class, this.getClass());
    }

}
