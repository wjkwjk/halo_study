package com.wjk.halo.service;

import com.wjk.halo.model.entity.ThemeSetting;
import com.wjk.halo.service.base.CrudService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ThemeSettingService extends CrudService<ThemeSetting, Integer> {

    @NonNull
    Map<String, Object> listAsMapBy(@NonNull String themeId);


    @NonNull
    List<ThemeSetting> listBy(String themeId);

    @Nullable
    @Transactional
    ThemeSetting save(@NonNull String key, @Nullable String value, @NonNull String themeId);

    @Transactional
    void save(@Nullable Map<String, Object> settings, @NonNull String themeId);


}
