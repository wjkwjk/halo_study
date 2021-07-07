package com.wjk.halo.model.enums;

public enum LogType implements ValueEnum<Integer>{

    POST_PUBLISHED(5),

    LOGIN_FAILED(35),

    LOGIN_IN(25),

    PROFILE_UPDATED(45)
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
