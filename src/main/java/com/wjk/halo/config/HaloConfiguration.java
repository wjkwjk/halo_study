package com.wjk.halo.config;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.cache.InMemoryCacheStore;
import com.wjk.halo.config.properties.HaloProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HaloProperties.class)
@Slf4j
public class HaloConfiguration {

    @Autowired
    private HaloProperties haloProperties;

    @Bean
    @ConditionalOnMissingBean
    public AbstractStringCacheStore stringCacheStore(){
        AbstractStringCacheStore stringCacheStore;
        switch (haloProperties.getCache()){
            case "memory":
            default:
                stringCacheStore = new InMemoryCacheStore();
                break;
        }
        log.info("Halo cache store load impl : [{}]", stringCacheStore.getClass());
        return stringCacheStore;
    }

}
