package com.wjk.halo.controller.admin.api;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.wjk.halo.annotation.DisableOnCondition;
import com.wjk.halo.cache.lock.CacheLock;
import com.wjk.halo.exception.BadRequestException;
import com.wjk.halo.model.dto.UserDTO;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.model.params.MultiFactorAuthParam;
import com.wjk.halo.model.params.PasswordParam;
import com.wjk.halo.model.params.UserParam;
import com.wjk.halo.model.support.BaseResponse;
import com.wjk.halo.model.support.UpdateCheck;
import com.wjk.halo.model.vo.MultiFactorAuthVO;
import com.wjk.halo.service.UserService;
import com.wjk.halo.utils.TwoFactorAuthUtils;
import com.wjk.halo.utils.ValidationUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //不带注解的参数获取来自请求链接中的参数，参数名赋值给对象中的属性名相同的变量

    @GetMapping("profiles")
    public UserDTO getProfile(User user){
        return new UserDTO().convertFrom(user);
    }

    @PutMapping("profiles")
    @DisableOnCondition
    public UserDTO updateProfile(@RequestBody UserParam userParam, User user){
        ValidationUtils.validate(userParam, UpdateCheck.class);

        userParam.update(user);

        return new UserDTO().convertFrom(userService.update(user));
    }
    @PutMapping("profiles/password")
    @DisableOnCondition
    public BaseResponse<String> updatePassword(@RequestBody @Valid PasswordParam passwordParam, User user){
        userService.updatePassword(passwordParam.getOldPassword(), passwordParam.getNewPassword(), user.getId());
        return BaseResponse.ok("密码修改成功");
    }
    //生成二维码图片
    @PutMapping("mfa/generate")
    @DisableOnCondition
    public MultiFactorAuthVO generateMFAQrImage(@RequestBody MultiFactorAuthParam multiFactorAuthParam, User user){
        if (MFAType.NONE == user.getMfaType()){
            if (MFAType.TFA_TOTP == multiFactorAuthParam.getMfaType()){
                String mfaKey = TwoFactorAuthUtils.generateTFAKey();
                String optAuthUrl = TwoFactorAuthUtils.generateOtpAuthUrl(user.getNickname(), mfaKey);
                String qrImageBase64 = "data:image/png;base64," +
                        Base64.encode(QrCodeUtil.generatePng(optAuthUrl, 128, 128));
                return new MultiFactorAuthVO(qrImageBase64, optAuthUrl, mfaKey, MFAType.TFA_TOTP);
            }else {
                throw new BadRequestException("暂不支持的 MFA 认证的方式");
            }
        }else {
            throw new BadRequestException("MFA 认证已启用，无需重复操作");
        }
    }

    @PutMapping("mfa/update")
    @CacheLock(autoDelete = false, prefix = "mfa")
    @DisableOnCondition
    public MultiFactorAuthVO updateMFAuth(@RequestBody @Valid MultiFactorAuthParam multiFactorAuthParam, User user){
        if (StrUtil.isNotBlank(user.getMfaKey()) && MFAType.useMFA(multiFactorAuthParam.getMfaType())){
            return new MultiFactorAuthVO(MFAType.TFA_TOTP);
        }else if (StrUtil.isBlank(user.getMfaKey()) && !MFAType.useMFA(multiFactorAuthParam.getMfaType())){
            return new MultiFactorAuthVO(MFAType.NONE);
        }else {
            final String tfaKey = StrUtil.isNotBlank(user.getMfaKey()) ? user.getMfaKey() : multiFactorAuthParam.getMfaKey();
            TwoFactorAuthUtils.validateTFACode(tfaKey, multiFactorAuthParam.getAuthcode());
        }
        User updateUser = userService.updateMFA(multiFactorAuthParam.getMfaType(), multiFactorAuthParam.getMfaKey(), user.getId());
        return new MultiFactorAuthVO(updateUser.getMfaType());
    }

}
