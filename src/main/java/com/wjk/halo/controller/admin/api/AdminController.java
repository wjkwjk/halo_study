package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.properties.PrimaryProperties;
import com.wjk.halo.service.AdminService;
import com.wjk.halo.service.OptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
