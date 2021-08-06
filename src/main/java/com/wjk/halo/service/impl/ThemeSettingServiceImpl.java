package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.ThemeSetting;
import com.wjk.halo.repository.ThemeSettingRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.ThemeService;
import com.wjk.halo.service.ThemeSettingService;
import com.wjk.halo.service.base.AbstractCrudService;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ThemeSettingServiceImpl extends AbstractCrudService<ThemeSetting, Integer> implements ThemeSettingService {

    private final ThemeSettingRepository themeSettingRepository;

    private final ThemeService themeService;

    private final Configuration configuration;

    public ThemeSettingServiceImpl(ThemeSettingRepository themeSettingRepository,
                                   ThemeService themeService,
                                   Configuration configuration) {
        super(themeSettingRepository);
        this.themeSettingRepository = themeSettingRepository;
        this.themeService = themeService;
        this.configuration = configuration;
    }
}
