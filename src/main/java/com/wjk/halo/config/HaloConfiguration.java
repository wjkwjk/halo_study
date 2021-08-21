package com.wjk.halo.config;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.cache.InMemoryCacheStore;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * 用来生成通用的客户端请求工具
 *
 * 主要用来在adminService中，向githup请求，以获该系统的最新版本
 */


@Configuration
@EnableConfigurationProperties(HaloProperties.class)
@Slf4j
public class HaloConfiguration {

    @Autowired
    private HaloProperties haloProperties;

    /**
     *  RestTemplate是一个同步的Web http客户端请求模版工具，spring框架做的抽象，但是本身并不具备发送请求的作用，
     *      底层还是需要指定客户端请求工具，例如HttpClient
     * RestTemplate 对象在底层通过使用 java.net 包下的实现创建 HTTP 请求，
     * 可以通过使用 ClientHttpRequestFactory 指定不同的HTTP请求方式。
     * 默认使用 SimpleClientHttpRequestFactory，是 ClientHttpRequestFactory 实现类
     */
    @Bean
    public RestTemplate httpsRestTemplate(RestTemplateBuilder builder) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException{
        RestTemplate httpsRestTemplate = builder.build();
        /**
         * 指定RestTemplate的客户端请求引擎
         */
        httpsRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClientUtils.createHttpsClient(
                (int) haloProperties.getDownloadTimeout().toMillis())));
        return httpsRestTemplate;
    }

    /**
     * 如果已经有某个类型的Bean了，就不创建，否则创建
     * 根据配置选择使用内存还是redis作为缓存
     * @return
     */

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
