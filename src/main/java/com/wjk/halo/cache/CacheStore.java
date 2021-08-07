package com.wjk.halo.cache;

import com.sun.istack.NotNull;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface CacheStore<K,V> {

    //存入某个键，以及值，超时时间
    void put(@NonNull K key, @NonNull V value, long timeout, @NonNull TimeUnit timeUnit);

    void put(@NonNull K key, @NonNull V value);

    //获得某个键
    @NonNull
    Optional<V> get(@NonNull K key);

    //删除键
    void delete(@NonNull K key);

    Boolean putIfAbsent(@NonNull K key, @NonNull V value, long timeout, @NonNull TimeUnit timeUnit);

}
