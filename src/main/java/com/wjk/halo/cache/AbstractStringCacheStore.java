package com.wjk.halo.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.istack.NotNull;
import com.wjk.halo.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractStringCacheStore extends AbstractCacheStore<String, String>{

    //将键值信息存放到缓存中
    public <T> void putAny(@NonNull String key, @NonNull T value, long timeout, @NonNull TimeUnit timeUnit){
        try {
            put(key, JsonUtils.objectToJson(value), timeout, timeUnit);
        }catch (JsonProcessingException e){
            throw new ServiceException("Failed to convert " + value + " to json", e);
        }
    }

    public <T> void putAny(String key, T value){
        try {
            put(key, JsonUtils.objectToJson(value));
        }catch (JsonProcessingException e){
            throw new ServiceException("Failed to convert " + value + " to json", e);
        }

    }

    //将从缓存中得到的String类型json反序列化成type类型对象并返回
    public <T> Optional<T> getAny(String key, Class<T> type){
        return get(key).map(value -> {
            try {
                return JsonUtils.jsonToObject(value, type);
            } catch (IOException e) {
                log.error("Failed to convert json to type: " + type.getName(), e);
                return null;
            }
        });
    }
}
