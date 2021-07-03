package com.wjk.halo.security.authentication;

import com.wjk.halo.security.support.UserDetail;
import org.springframework.lang.NonNull;

public interface Authentication {

    @NonNull
    UserDetail getDetail();

}
