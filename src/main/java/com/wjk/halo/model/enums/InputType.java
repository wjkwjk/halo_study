package com.wjk.halo.model.enums;

import org.springframework.lang.Nullable;

public enum InputType {

    TEXT,

    SWITCH

    ;
    public static InputType typeOf(@Nullable Object type){
        if (type != null){
            for (InputType inputType : values()){
                if (inputType.name().equalsIgnoreCase(type.toString())){
                    return inputType;
                }
            }
        }
        return TEXT;
    }

}
