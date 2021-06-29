package com.wjk.halo.cache;

import com.wjk.halo.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractCacheStore<K,V> implements CacheStore<K,V> {

    //将键独立开来，将值，超时时间，时间单位包装成一个整体作为值，使用CacheWrapper进行包装
    @Override
    public void put(K key, V value, long timeout, TimeUnit timeUnit) {
        putInternal(key, buildCacheWrapper(value, timeout, timeUnit));
    }

    //存放键值
    abstract void putInternal(@NonNull K key, @NonNull CacheWrapper<V> cacheWrapper);

    //没有给出超时时间
    @Override
    public void put(K key, V value){
        putInternal(key, buildCacheWrapper(value, 0, null));
    }

    //将值，超时时间，时间单位包装成一个CacheWrapper对象
    @NonNull
    private CacheWrapper<V> buildCacheWrapper(@NonNull V value, long timeout, @NonNull TimeUnit timeUnit){
        Date now = DateUtils.now();

        Date expireAt = null;

        if (timeout > 0 && timeUnit != null){
            expireAt = DateUtils.add(now, timeout, timeUnit);
        }

        CacheWrapper<V> cacheWrapper = new CacheWrapper<>();
        cacheWrapper.setCreateAt(now);
        cacheWrapper.setExpireAt(expireAt);
        cacheWrapper.setData(value);
        return cacheWrapper;
    }

    //根据键取值
    @NonNull
    abstract Optional<CacheWrapper<V>> getInternal(@NonNull K key);

    //根据键取值，并且进行过期判断，判断则删除键，否则返回值
    @Override
    public Optional<V> get(K key){
        return getInternal(key).map(cacheWrapper -> {
            if (cacheWrapper.getExpireAt() != null && cacheWrapper.getExpireAt().before(DateUtils.now())){
                log.warn("Cache key: [{}] has been expired", key);
                delete(key);

                return null;
            }
            return cacheWrapper.getData();
        });
    }



}
