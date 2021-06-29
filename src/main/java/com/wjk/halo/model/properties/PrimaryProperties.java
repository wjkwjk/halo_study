package com.wjk.halo.model.properties;

public enum PrimaryProperties implements PropertyEnum{

    IS_INSTALLED("is_installed", Boolean.class, "false"),
    ;


    private final String value;
    private Class<?> type;
    private String defaultValue;

    PrimaryProperties(String value, Class<?> type, String defaultValue) {
        this.value = value;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getValue() {
        return value;
    }
}
