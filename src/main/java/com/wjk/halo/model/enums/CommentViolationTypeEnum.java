package com.wjk.halo.model.enums;

import lombok.Getter;

@Getter
public enum CommentViolationTypeEnum {
    NORMAL(0),
    FREQUENTLY(1)
    ;

    private final int type;

    CommentViolationTypeEnum(int type) {
        this.type = type;
    }
}
