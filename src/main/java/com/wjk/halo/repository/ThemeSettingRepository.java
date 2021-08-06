package com.wjk.halo.repository;

import com.wjk.halo.model.entity.ThemeSetting;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ThemeSettingRepository extends BaseRepository<ThemeSetting, Integer> {

    @NonNull
    List<ThemeSetting> findAllByThemeId(@NonNull String themeId);

    @NonNull
    Optional<ThemeSetting> findByThemeIdAndKey(@NonNull String themeId, @NonNull String key);

    void deleteByThemeId(String themeId);
}
