package com.wjk.halo.service;

import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.params.LoginParam;
import com.wjk.halo.security.token.AuthToken;
import org.springframework.lang.NonNull;

public interface AdminService {

    @NonNull
    User authenticate(@NonNull LoginParam loginParam);

    @NonNull
    AuthToken authCodeCheck(@NonNull LoginParam loginParam);

}
