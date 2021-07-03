package com.wjk.halo.model.enums;

public enum PostEditorType implements ValueEnum<Integer>{

    MARKDOWN(0),
    RICHTEXT(1),
    ;

    private final Integer value;

    PostEditorType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
