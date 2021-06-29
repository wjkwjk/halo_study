package com.wjk.halo.utils;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceUtils {
    private ServiceUtils(){}

    @NonNull
    public static <ID, T> Set<ID> fetchProperty(final Collection<T> datas, Function<T, ID> mappingFunction){
        return CollectionUtils.isEmpty(datas) ?
                Collections.emptySet() :
                datas.stream().map(mappingFunction).collect(Collectors.toSet());
    }

    @NonNull
    public static <ID, D> Map<ID, D> convertToMap(Collection<D> list, Function<D, ID> mappingFunction){
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }

        Map<ID, D> resultMap = new HashMap<>();

        list.forEach(data->resultMap.putIfAbsent(mappingFunction.apply(data), data));

        return resultMap;
    }

    @NonNull
    public static <ID, D, V> Map<ID, V> convertToMap(@Nullable Collection<D> list, @NonNull Function<D, ID> keyFunction, @NonNull Function<D,V> valueFunction){
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }

        Map<ID, V> resultMap = new HashMap<>();

        list.forEach(data -> resultMap.putIfAbsent(keyFunction.apply(data), valueFunction.apply(data)));

        return resultMap;

    }


}
