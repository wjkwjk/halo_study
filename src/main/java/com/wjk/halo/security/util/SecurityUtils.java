package com.wjk.halo.security.util;

import com.wjk.halo.model.entity.User;
import org.springframework.lang.NonNull;

public class SecurityUtils {
    private final static String TOKEN_ACCESS_CACHE_PREFIX = "halo.admin.access.token.";
    private final static String TOKEN_REFRESH_CACHE_PREFIX = "halo.admin.refresh.token.";
    private final static String ACCESS_TOKEN_CACHE_PREFIX = "halo.admin.access_token.";
    private final static String REFRESH_TOKEN_CACHE_PREFIX = "halo.admin.refresh_token.";

    private SecurityUtils(){}

    @NonNull
    public static String buildAccessTokenKey(@NonNull User user){
        return ACCESS_TOKEN_CACHE_PREFIX + user.getId();
    }

    @NonNull
    public static String buildRefreshTokenKey(@NonNull User user){
        return REFRESH_TOKEN_CACHE_PREFIX + user.getId();
    }

    @NonNull
    public static String buildTokenAccessKey(@NonNull String accessToken){
        return TOKEN_ACCESS_CACHE_PREFIX + accessToken;
    }

    @NonNull
    public static String buildTokenRefreshKey(@NonNull String refreshToken){
        return TOKEN_REFRESH_CACHE_PREFIX + refreshToken;
    }

}
