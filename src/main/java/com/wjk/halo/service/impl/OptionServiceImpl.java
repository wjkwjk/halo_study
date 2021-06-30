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

    @Override
    public <T> T getByPropertyOrDefault(PropertyEnum property, Class<T> propertyType, T defaultValue) {
        // 调用getByProperty进行查找，如果查找结果为空，则返回默认值
        return getByProperty(property, propertyType).orElse(defaultValue);
    }

    //获取结果并将结果转化为指定的类型
    @Override
    public <T> Optional<T> getByProperty(PropertyEnum property, Class<T> propertyType) {
        return getByProperty(property).map(propertyValue->PropertyEnum.convertTo(propertyValue.toString(), propertyType));
    }


    @Override
    public Optional<Object> getByProperty(PropertyEnum property) {
        //property.getValue()结果为 "is_installed"
        //返回：Optional[false]
        return getByKey(property.getValue());
    }

    @Override
    public Optional<Object> getByKey(String key){
        return Optional.ofNullable(listOptions().get(key));
    }

    //根据key先从缓存中获取数据
    @Override
    public Map<String, Object> listOptions(){
        return cacheStore.getAny(OPTIONS_KEY, Map.class).orElseGet(()->{
            List<Option> options = listAll();
            //得到所有key集合
            Set<String> keys = ServiceUtils.fetchProperty(options, Option::getKey);

            Map<String, Object> userDefinedOptionMap = ServiceUtils.convertToMap(options, Option::getKey, option -> {
                String key = option.getKey();
                PropertyEnum propertyEnum = propertyEnumMap.get(key);

                if (propertyEnum == null){
                    return option.getValue();
                }
                return PropertyEnum.convertTo(option.getValue(), propertyEnum);
            });

            Map<String, Object> result = new HashMap<>(userDefinedOptionMap);

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
            cacheStore.putAny(OPTIONS_KEY, result);

            //返回HashMap类型的{"is_installed":false}
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
