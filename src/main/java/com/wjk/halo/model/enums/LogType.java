package com.wjk.halo.model.enums;

public enum LogType implements ValueEnum<Integer>{

    BLOG_INITIALIZED(0),

    POST_PUBLISHED(5),

    LOGIN_FAILED(35),

    LOGIN_IN(25),

    PROFILE_UPDATED(45),

    SHEET_PUBLISHED(50),

    LOGGED_OUT(30)
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
