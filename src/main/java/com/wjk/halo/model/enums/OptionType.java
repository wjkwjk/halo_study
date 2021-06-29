package com.wjk.halo.model.enums;

public enum OptionType implements ValueEnum<Integer>{
    INTERNAL(0),

    CUSTOM(1);

    private final Integer value;

    OptionType(Integer value){
        this.value = value;
    }


    @Override
    public Integer getValue() {
        return value;
    }
}
