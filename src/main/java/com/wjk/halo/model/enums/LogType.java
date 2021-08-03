package com.wjk.halo.model.enums;

public enum LogType implements ValueEnum<Integer>{

    BLOG_INITIALIZED(0),

    POST_PUBLISHED(5),

    POST_EDITED(15),

    LOGIN_FAILED(35),

    POST_DELETED(20),

    LOGIN_IN(25),

    PASSWORD_UPDATED(40),

    PROFILE_UPDATED(45),

    SHEET_PUBLISHED(50),

    SHEET_EDITED(55),

    LOGGED_OUT(30),

    SHEET_DELETED(60),

    MFA_UPDATED(65)
    ;

    private final Integer value;

    LogType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
