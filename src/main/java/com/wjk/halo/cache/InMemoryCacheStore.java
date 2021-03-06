package com.wjk.halo.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class InMemoryCacheStore extends AbstractStringCacheStore{

    private final Timer timer;

    private final static long PERIOD = 60 * 1000;

    private final Lock lock = new ReentrantLock();

    /**
     * scheduleAtFixedRate(TimerTask task, long delay, long period)：定时执行任务
     *  task : 定时执行的任务
     *  delay : 延迟多少时间后，开始定时执行任务
     *  period : 两次任务执行之间的最小事时间间隔
     *
     *
     * Timer是一种定时器工具，用来在一个后台线程计划执行指定任务。它可以计划执行一个任务一次或反复多次。
     * TimerTask一个抽象类，它的子类代表一个可以被Timer计划的任务。
     */
    public InMemoryCacheStore(){
        timer = new Timer();
        /**
         * 每隔60s删除那些过期的键
         */
        timer.scheduleAtFixedRate(new CacheExpiryCleaner(), 0, PERIOD);
    }

    //使用ConcurrentHashMap保存身份信息
    private final static ConcurrentHashMap<String, CacheWrapper<String>> CACHE_CONTAINER = new ConcurrentHashMap<>();

    //将信息保存在ConcurrentHashMap
    @Override
    void putInternal(String key, CacheWrapper<String> cacheWrapper) {
        CacheWrapper<String> putCacheWrapper = CACHE_CONTAINER.put(key, cacheWrapper);
        log.debug("Put [{}] cache result: [{}], original cache wrapper: [{}]", key, putCacheWrapper, cacheWrapper);
    }

    /**
     *
     * @param key   "options"(String类型)
     * @return  返回结果应为CacheWrapper类型对象，但是由于此次不存在key=options，因此返回null
     */
    //根据键从ConcurrentHashMap取值
    @Override
    Optional<CacheWrapper<String>> getInternal(String key) {
        return Optional.ofNullable(CACHE_CONTAINER.get(key));
    }

    @Override
    Boolean putInternalIfAbsent(String key, CacheWrapper<String> cacheWrapper) {

        log.debug("Preparing to put key: [{}], value: [{}]", key, cacheWrapper);

        lock.lock();
        try {
            Optional<String> valueOptional = get(key);

            if (valueOptional.isPresent()){
                log.warn("Failed to put the cache, because the key: [{}] has been present already", key);
                return false;
            }

            putInternal(key, cacheWrapper);
            log.debug("Put successfully");
            return true;
        }finally {
            lock.unlock();
        }

    }

    @Override
    public void delete(String key){
        CACHE_CONTAINER.remove(key);
        log.debug("Removed key:[{}]", key);
    }

    /**
     * 需要定时执行的任务
     * 定时删除那些已经过期的键
     */
    private class CacheExpiryCleaner extends TimerTask {

        @Override
        public void run() {
            CACHE_CONTAINER.keySet().forEach(key -> {
                if (!InMemoryCacheStore.this.get(key).isPresent()) {
                    log.debug("Deleted the cache: [{}] for expiration", key);
                }
            });
        }
    }

}
