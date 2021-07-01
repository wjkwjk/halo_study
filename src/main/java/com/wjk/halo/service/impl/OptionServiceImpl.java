package com.wjk.halo.service.impl;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.model.entity.Option;
import com.wjk.halo.model.properties.PropertyEnum;
import com.wjk.halo.repository.OptionRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class OptionServiceImpl extends AbstractCrudService<Option, Integer> implements OptionService {

    private final OptionRepository optionRepository;
    private final AbstractStringCacheStore cacheStore;
    private final Map<String, PropertyEnum> propertyEnumMap;

    //抽象类对象作为方法参数有问题？
    public OptionServiceImpl(OptionRepository optionRepository, AbstractStringCacheStore cacheStore) {
        super(optionRepository);
        this.optionRepository = optionRepository;
        this.cacheStore = cacheStore;

        propertyEnumMap = Collections.unmodifiableMap(PropertyEnum.getValuePropertyEnumMap());
    }

    /**
     *
     * @param property  需要查找的数据的名称 IS_INSTALLED(PropertyEnum类型)
     * @param propertyType  需要查找的数据的类型  Booolean
     * @param defaultValue  默认类型    false(Boolean类型)
     * @param <T>
     * @return
     */
    @Override
    public <T> T getByPropertyOrDefault(PropertyEnum property, Class<T> propertyType, T defaultValue) {
        // 调用getByProperty进行查找，如果查找结果为空，则返回默认值
        return getByProperty(property, propertyType).orElse(defaultValue);
    }

    /**
     *
     * @param property  IS_INSTALLED(PropertyEnum类型)
     * @param propertyType  Boolean
     * @param <T>
     * @return
     */
    //获取结果并将结果转化为指定的类型
    @Override
    public <T> Optional<T> getByProperty(PropertyEnum property, Class<T> propertyType) {
        return getByProperty(property).map(propertyValue->PropertyEnum.convertTo(propertyValue.toString(), propertyType));
    }

    /**
     *
     * @param property  IS_INSTALLED(PropertyEnum类型)
     * @return
     */
    @Override
    public Optional<Object> getByProperty(PropertyEnum property) {
        //property.getValue()结果为 "is_installed"
        //返回：Optional[false]
        return getByKey(property.getValue());
    }

    /**
     *  从缓存中（缓存中没有则从数据库中）拿对应key的value
     * @param key   "is_installed"(String类型)
     * @return
     */
    @Override
    public Optional<Object> getByKey(String key){
        return Optional.ofNullable(listOptions().get(key));
    }

    /**
     *  此返回式表示先从缓存中查找，若缓存中没有，则再从数据中查找，listAll()表示从数据库中查找所有记录，并且将数据库查询得到的键值信息存入缓存中
     * @return  将一个String类型的字符串转变为对应的枚举中存储的类型
     */
    //根据key先从缓存中获取数据
    @Override
    public Map<String, Object> listOptions(){
        return cacheStore.getAny(OPTIONS_KEY, Map.class).orElseGet(()->{
            List<Option> options = listAll();
            //在数据库返回结果中返回所有列名为key的值组成的集合
            Set<String> keys = ServiceUtils.fetchProperty(options, Option::getKey);
            //返回数据库查询结果中提取出来的键值对，键值分别为key属性个value属性
            Map<String, Object> userDefinedOptionMap = ServiceUtils.convertToMap(options, Option::getKey, option -> {
                String key = option.getKey();
                PropertyEnum propertyEnum = propertyEnumMap.get(key);

                if (propertyEnum == null){
                    return option.getValue();
                }
                return PropertyEnum.convertTo(option.getValue(), propertyEnum);
            });

            Map<String, Object> result = new HashMap<>(userDefinedOptionMap);
            //如果存在某个key不位于缓存中，则将放入到缓存中
            propertyEnumMap.keySet()
                    .stream()
                    .filter(key -> !keys.contains(key))
                    .forEach(key -> {
                        PropertyEnum propertyEnum = propertyEnumMap.get(key);

                        if (StringUtils.isBlank(propertyEnum.defaultValue())){
                            return;
                        }

                        result.put(key, PropertyEnum.convertTo(propertyEnum.defaultValue(), propertyEnum));

                    });
            cacheStore.putAny(OPTIONS_KEY, result);//作用是将信息保存到缓存中，其中键为"options",值为数据库查询得到的所有键值对

            //返回HashMap类型的，数据库查询得到的所有键值对
            return result;
        });
    }

    @Override
    public List<Option> listAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Option> listAll(Pageable pageable) {
        return null;
    }
}
