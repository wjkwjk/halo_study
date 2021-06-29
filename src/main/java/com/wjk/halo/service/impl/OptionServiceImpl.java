package com.wjk.halo.service.impl;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.model.entity.Option;
import com.wjk.halo.model.properties.PropertyEnum;
import com.wjk.halo.repository.OptionRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class OptionServiceImpl extends AbstractCrudService<Option, Integer> implements OptionService {

    private final OptionRepository optionRepository;
    private final AbstractStringCacheStore cacheStore;


    //抽象类对象作为方法参数有问题？
    public OptionServiceImpl(OptionRepository optionRepository,AbstractStringCacheStore cacheStore) {
        super(optionRepository);
        this.optionRepository = optionRepository;
        this.cacheStore = cacheStore;
    }

    @Override
    public <T> T getByPropertyOrDefault(PropertyEnum property, Class<T> propertyType, T defaultValue) {
        // 调用getByProperty进行查找，如果查找结果为空，则返回默认值
        return getByProperty(property, propertyType).orElse(defaultValue);
    }

    @Override
    public <T> Optional<T> getByProperty(PropertyEnum property, Class<T> propertyType) {
        return getByProperty(property).map(propertyValue->PropertyEnum.convertTo(propertyValue.toString(), propertyType));
    }

    @Override
    public Optional<Object> getByProperty(PropertyEnum property) {
        return getByKey(property.getValue());
    }


    public Optional<Object> getByKey(String key){
        return Optional.ofNullable(listOptions().get(key));
    }

    public Map<String, Object> listOptions(){
        return null;
    }

}
