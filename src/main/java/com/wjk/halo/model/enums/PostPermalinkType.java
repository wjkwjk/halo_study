package com.wjk.halo.model.enums;

public enum PostPermalinkType implements ValueEnum<Integer>{
    /**
     * /archives/${slug}
     */
    DEFAULT(0),

    /**
     * /1970/01/01/${slug}
     */
    DATE(1),

    /**
     * /1970/01/${slug}
     */
    DAY(2),

    /**
     * /?p=${id}
     */
    ID(3),

    /**
     * /1970/${slug}
     */
    YEAR(4);

    private final Integer value;

    PostPermalinkType(Integer value) {
        this.value = value;
    }


    @Override
    public Integer getValue() {
        return value;
    }
}
