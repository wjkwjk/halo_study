package com.wjk.halo.model.params;

import com.wjk.halo.model.enums.MFAType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class MultiFactorAuthParam {
    private MFAType mfaType = MFAType.NONE;

    private String mfaKey;

    @NotBlank(message = "MFA Code不能为空")
    @Size(min = 6, max = 6, message = "MFA Code应为 {max} 位")
    private String authcode;
}
