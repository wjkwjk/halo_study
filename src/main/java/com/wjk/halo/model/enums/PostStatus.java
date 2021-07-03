package com.wjk.halo.model.enums;

public enum PostStatus implements ValueEnum<Integer>{

    PUBLISHED(0),
    DRAFT(1),
    RECYCLE(2),
    INTIMATE(3)
    ;

    private final int value;

    PostStatus(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
