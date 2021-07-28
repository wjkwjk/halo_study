package com.wjk.halo.security.service.impl;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.security.service.OneTimeTokenService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OneTimeTokenServiceImpl implements OneTimeTokenService {

    public static final int OTT_EXPIRED_DAY = 1;
    private final AbstractStringCacheStore cacheStore;

    public OneTimeTokenServiceImpl(AbstractStringCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    //从缓存中取token
    @Override
    public Optional<String> get(String oneTimeToken) {
        return cacheStore.get(oneTimeToken);
    }

    @Override
    public void revoke(String oneTimeToken) {
        cacheStore.delete(oneTimeToken);
    }
}
