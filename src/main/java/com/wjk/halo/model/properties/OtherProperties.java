package com.wjk.halo.model.properties;

public enum OtherProperties implements PropertyEnum{

    GLOBAL_ABSOLUTE_PATH_ENABLED("global_absolute_path_enabled", Boolean.class, "true"),

    ;

    private final String value;
    private final Class<?> type;
    private final String defaultValue;

    OtherProperties(String value, Class<?> type, String defaultValue) {
        this.value = value;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }
}
