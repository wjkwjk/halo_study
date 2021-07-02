package com.wjk.halo.model.enums;

public enum MFAType implements ValueEnum<Integer>{
    NONE(0),

    TFA_TOTP(1)
    ;

    private final Integer value;

    MFAType(Integer value) {
        this.value = value;
    }

    public static boolean useMFA(MFAType mfaType){
        return mfaType != null && MFAType.NONE != mfaType;
    }

    @Override
    public Integer getValue() {
        return null;
    }
}
