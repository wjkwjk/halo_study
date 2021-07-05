package com.wjk.halo.model.enums;

public enum TimeUnit implements ValueEnum<Integer>{

    DAY(0),
    HOUR(1);

    private final Integer value;

    TimeUnit(Integer value) {
        this.value = value;
    }


    @Override
    public Integer getValue() {
        return value;
    }
}
