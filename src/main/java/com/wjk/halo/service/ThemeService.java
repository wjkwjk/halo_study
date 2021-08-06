package com.wjk.halo.service;

import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import com.wjk.halo.model.support.ThemeFile;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ThemeService {

    String THEMES_CACHE_KEY = "themes";

    String[] SETTINGS_NAMES = {"settings.yaml", "settings.yml"};

    String THEME_FOLDER = "templates/themes";

    String CUSTOM_SHEET_PREFIX = "sheet_";

    /**
     * The type of file that can be modified.
     */
    String[] CAN_EDIT_SUFFIX = {".ftl", ".css", ".js", ".yaml", ".yml", ".properties"};


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

    @NonNull
    @Deprecated
    ThemeProperty getThemeOfNonNullBy(@NonNull String themeId);

    @NonNull
    ThemeProperty getActivatedTheme();

    @NonNull
    List<ThemeFile> listThemeFolderBy(@NonNull String themeId);

    String getTemplateContent(@NonNull String absolutePath);

    String getTemplateContent(@NonNull String themeId, @NonNull String absolutePath);

    void saveTemplateContent(@NonNull String absolutePath, @NonNull String content);

    void saveTemplateContent(@NonNull String themeId, @NonNull String absolutePath, @NonNull String content);

    @Deprecated
    @NonNull
    List<String> listCustomTemplates(@NonNull String themeId);

    @NonNull
    List<String> listCustomTemplates(@NonNull String themeId, @NonNull String prefix);

}
