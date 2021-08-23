package com.wjk.halo.security.authentication;

import com.wjk.halo.security.support.UserDetail;

/**
 * 验证类，将用户的信息作为验证信息
 *  包装了UserDetail
 */
public class AuthenticationImpl implements Authentication {

    private final UserDetail userDetail;

    public AuthenticationImpl(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    @Override
    public UserDetail getDetail() {
        return userDetail;
    }
}
