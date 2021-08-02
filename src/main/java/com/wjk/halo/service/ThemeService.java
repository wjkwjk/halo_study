package com.wjk.halo.service;

import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ThemeService {

    String THEMES_CACHE_KEY = "themes";

    String[] SETTINGS_NAMES = {"settings.yaml", "settings.yml"};

    String THEME_FOLDER = "templates/themes";

    boolean templateExists(@Nullable String template);

    @NonNull
    Optional<ThemeProperty> fetchActivatedTheme();

    @NonNull
    Optional<ThemeProperty> fetchThemePropertyBy(@Nullable String themeId);

    @NonNull
    List<ThemeProperty> getThemes();

    Path getBasePath();

    @NonNull
    String getActivatedThemeId();

    @lombok.NonNull
    @Deprecated
    ThemeProperty getThemeOfNonNullBy(@lombok.NonNull String themeId);
}
