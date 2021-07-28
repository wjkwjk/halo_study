package com.wjk.halo.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.Nullable;

public enum Mode {

    PRODUCTION,

    DEMO,

    TEST
    ;
    @JsonCreator
    public static Mode valueFrom(@Nullable String value) {
        Mode modeResult = null;
        for (Mode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                modeResult = mode;
                break;
            }
        }
        if (modeResult == null) {
            modeResult = PRODUCTION;
        }
        return modeResult;
    }

    @JsonValue
    String getValue() {
        return this.name().toLowerCase();
    }
}
