package com.wjk.halo.controller.admin.api;

import com.wjk.halo.annotation.DisableOnCondition;
import com.wjk.halo.cache.lock.CacheLock;
import com.wjk.halo.model.dto.EnvironmentDTO;
import com.wjk.halo.model.dto.LoginPreCheckDTO;
import com.wjk.halo.model.dto.StatisticDTO;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.model.params.LoginParam;
import com.wjk.halo.model.params.ResetPasswordParam;
import com.wjk.halo.model.properties.PrimaryProperties;
import com.wjk.halo.model.support.BaseResponse;
import com.wjk.halo.security.token.AuthToken;
import com.wjk.halo.service.AdminService;
import com.wjk.halo.service.OptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final OptionService optionService;


    public AdminController(AdminService adminService, OptionService optionService) {
        this.adminService = adminService;
        this.optionService = optionService;
    }


    @GetMapping(value = "/is_installed")
    public boolean isInstall(){
        return optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);
    }

    @PostMapping(value = "login/precheck")
    @CacheLock(autoDelete = false, prefix = "login_precheck")
    public LoginPreCheckDTO authPreCheck(@RequestBody @Valid LoginParam loginParam){
        final User user = adminService.authenticate(loginParam);
        //返回一个bool值，表示是否启用MFA，不管是false还是true,都能表示precheck通过
        return new LoginPreCheckDTO(MFAType.useMFA(user.getMfaType()));
    }

    @PostMapping(value = "login")
    @CacheLock(autoDelete = false, prefix = "login_auth")
    public AuthToken auth(@RequestBody @Valid LoginParam loginParam){
        return adminService.authCodeCheck(loginParam);
    }

    /**
     * 有一个拦截器，因此需要先进行登陆，得到token，再将token放到请求参数中
     * ADMIN-Authorization：token，放在请求头中
     * admin_token：token，放在请求参数中
     */
    @PostMapping("logout")
    @CacheLock(autoDelete = false)
    public void logout(){
        adminService.clearToken();
    }

    @PostMapping("password/code")
    @CacheLock(autoDelete = false)
    @DisableOnCondition
    public void sendResetCode(@RequestBody @Valid ResetPasswordParam param){
        adminService.sendResetPasswordCode(param);
    }

    @PutMapping("password/reset")
    @CacheLock(autoDelete = false)
    @DisableOnCondition
    public void resetPassword(@RequestBody @Valid ResetPasswordParam param){
        adminService.resetPasswordByCode(param);
    }

    @PostMapping("refresh/{refreshToken}")
    @CacheLock(autoDelete = false)
    public AuthToken refresh(@PathVariable("refreshToken") String refreshToken){
        return adminService.refreshToken(refreshToken);
    }

    @GetMapping("counts")
    @Deprecated
    public StatisticDTO getCount(){
        return adminService.getCount();
    }

    @GetMapping("environments")
    public EnvironmentDTO getEnvironments(){
        return adminService.getEnvironments();
    }

    @PutMapping("halo-admin")
    @Deprecated
    public void updateAdmin(){
        adminService.updateAdminAssets();
    }

    @GetMapping(value = "halo/logfile")
    @DisableOnCondition
    public BaseResponse<String> getLogFiles(@RequestParam("lines") Long lines){
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), adminService.getLogFiles(lines));
    }


}
