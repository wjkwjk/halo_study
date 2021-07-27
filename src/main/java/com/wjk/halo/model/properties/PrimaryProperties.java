package com.wjk.halo.model.properties;

import static com.wjk.halo.model.support.HaloConst.DEFAULT_THEME_ID;

public enum PrimaryProperties implements PropertyEnum{

    IS_INSTALLED("is_installed", Boolean.class, "false"),

    THEME("theme", String.class, DEFAULT_THEME_ID),

    BIRTHDAY("birthday", Long.class, ""),

    DEV_MODE("developer_mode", Boolean.class, "false"),

    DEFAULT_MENU_TEAM("default_menu_team", String.class, "")
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

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }
}
