package com.wjk.halo.service;

import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.params.LoginParam;
import com.wjk.halo.model.params.ResetPasswordParam;
import com.wjk.halo.security.token.AuthToken;
import org.springframework.lang.NonNull;

public interface AdminService {

    //token过期时间
    int ACCESS_TOKEN_EXPIRED_SECONDS = 24 * 3600;

    int REFRESH_TOKEN_EXPIRED_DAYS = 30;

    @NonNull
    User authenticate(@NonNull LoginParam loginParam);

    @NonNull
    AuthToken authCodeCheck(@NonNull LoginParam loginParam);

    void clearToken();

    void sendResetPasswordCode(@NonNull ResetPasswordParam param);

}
