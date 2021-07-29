package com.wjk.halo.model.vo;

import com.wjk.halo.model.enums.MFAType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MultiFactorAuthVO {
    private String qrImage;

    private String optAuthUrl;

    private String mfaKey;

    private MFAType mfaType;

    public MultiFactorAuthVO(MFAType mfaType) {
        this.mfaType = mfaType;
    }
}
