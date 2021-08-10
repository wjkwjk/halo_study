package com.wjk.halo.security.service;

import org.springframework.lang.NonNull;

import java.util.Optional;

//一次性token的获取和删除

public interface OneTimeTokenService {

    @NonNull
    Optional<String> get(@NonNull String oneTimeToken);

    void revoke(@NonNull String oneTimeToken);
}
