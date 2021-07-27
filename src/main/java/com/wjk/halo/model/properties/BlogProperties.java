package com.wjk.halo.model.properties;

public enum BlogProperties implements PropertyEnum{

    //博客位于的地点
    BLOG_LOCATE("blog_locate", String.class, ""),
    BLOG_TITLE("blog_title", String.class, ""),
    BLOG_LOGO("blog_logo", String.class, ""),
    BLOG_URL("blog_url", String.class, "")
    ;


    private final String value;
    private final Class<?> type;
    private final String defaultValue;

    BlogProperties(String value, Class<?> type, String defaultValue) {
        this.defaultValue = defaultValue;
        if (!PropertyEnum.isSupportedType(type)){
            throw new IllegalArgumentException("Unsupported blog property type: " + type);
        }
        this.value = value;
        this.type = type;

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
