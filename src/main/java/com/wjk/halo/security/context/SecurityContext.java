package com.wjk.halo.security.context;

import com.wjk.halo.security.authentication.Authentication;
import org.springframework.lang.Nullable;

public interface SecurityContext {
    @Nullable
    Authentication getAuthentication();

    void setAuthentication(@Nullable Authentication authentication);

    default boolean isAuthenticated(){
        return getAuthentication() != null;
    }

}
