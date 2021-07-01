package com.wjk.halo.utils;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceUtils {
    private ServiceUtils(){}


    /**
     *
     * @param datas 表示因为缓存中查询不到，所以去数据库中查找全部记录的返回值
     * @param mappingFunction   Option::getKey，即用来获取键
     * @param <ID>
     * @param <T>
     * @return  先判断数据库查找的返回值是否为空，若为空，则返回一个空的集合，否则返回由所有键值组成的集合
     */
    //从数据库查询结果中得到每行数据中列名为key的集合
    @NonNull
    public static <ID, T> Set<ID> fetchProperty(final Collection<T> datas, Function<T, ID> mappingFunction){
        return CollectionUtils.isEmpty(datas) ?
                Collections.emptySet() :
                datas.stream().map(mappingFunction).collect(Collectors.toSet());
    }

    /**
     *
     * @param list
     * @param mappingFunction
     * @param <ID>
     * @param <D>
     * @return
     */
    @NonNull
    public static <ID, D> Map<ID, D> convertToMap(Collection<D> list, Function<D, ID> mappingFunction){
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }

        Map<ID, D> resultMap = new HashMap<>();

        list.forEach(data->resultMap.putIfAbsent(mappingFunction.apply(data), data));

        return resultMap;
    }

    /**
     *
     * @param list  数据库的全部查询结果
     * @param keyFunction   获取数据库查询结果中，列名为key的值
     * @param valueFunction
     * @param <ID>
     * @param <D>
     * @param <V>
     * @return
     */
    @NonNull
    public static <ID, D, V> Map<ID, V> convertToMap(@Nullable Collection<D> list, @NonNull Function<D, ID> keyFunction, @NonNull Function<D,V> valueFunction){
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }

        Map<ID, V> resultMap = new HashMap<>();
        //resultMap存储的键值对为：数据库查询结果中的key属性和value属性
        list.forEach(data -> resultMap.putIfAbsent(keyFunction.apply(data), valueFunction.apply(data)));

        return resultMap;

    }


}
