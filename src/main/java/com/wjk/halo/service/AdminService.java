package com.wjk.halo.service;

import com.wjk.halo.model.dto.EnvironmentDTO;
import com.wjk.halo.model.dto.StatisticDTO;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.params.LoginParam;
import com.wjk.halo.model.params.ResetPasswordParam;
import com.wjk.halo.security.token.AuthToken;
import org.springframework.lang.NonNull;

public interface AdminService {

    //token过期时间
    int ACCESS_TOKEN_EXPIRED_SECONDS = 24 * 3600;

    int REFRESH_TOKEN_EXPIRED_DAYS = 30;

    String LOG_PATH = "logs/spring.log";

    @NonNull
    User authenticate(@NonNull LoginParam loginParam);

    @NonNull
    AuthToken authCodeCheck(@NonNull LoginParam loginParam);

    void clearToken();

    void sendResetPasswordCode(@NonNull ResetPasswordParam param);

    void resetPasswordByCode(@NonNull ResetPasswordParam param);

    @NonNull
    AuthToken refreshToken(@NonNull String refreshToken);

    @NonNull
    @Deprecated
    StatisticDTO getCount();

    @NonNull
    EnvironmentDTO getEnvironments();

    void updateAdminAssets();

    String getLogFiles(@NonNull Long lines);

}
