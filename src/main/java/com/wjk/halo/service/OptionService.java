package com.wjk.halo.service;

import com.sun.istack.internal.NotNull;
import com.wjk.halo.model.entity.Option;
import com.wjk.halo.model.properties.PropertyEnum;
import com.wjk.halo.service.base.CrudService;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface OptionService extends CrudService<Option, Integer> {

    /**
     *
     * @param property  需要查找的数据的名称
     * @param propertyType  需要查找的数据的类型
     * @param defaultValue  默认类型
     * @param <T>
     * @return
     */
    <T> T getByPropertyOrDefault(@NotNull PropertyEnum property, @NotNull Class<T> propertyType, T defaultValue);

    <T> Optional<T> getByProperty(@NonNull PropertyEnum property, @NonNull Class<T> propertyType);

    Optional<Object> getByProperty(@NonNull PropertyEnum property);

}
