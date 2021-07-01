package com.wjk.halo.controller.admin.api;

import com.wjk.halo.cache.lock.CacheLock;
import com.wjk.halo.model.dto.LoginPreCheckDTO;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.enums.MFAType;
import com.wjk.halo.model.params.LoginParam;
import com.wjk.halo.model.properties.PrimaryProperties;
import com.wjk.halo.service.AdminService;
import com.wjk.halo.service.OptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/admin/api")
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
        return new LoginPreCheckDTO(MFAType.useMFA(user.getMfaType()));
    }

}
