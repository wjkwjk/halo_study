package com.wjk.halo.service;

import com.wjk.halo.handler.theme.config.support.Group;
import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import com.wjk.halo.model.support.ThemeFile;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ThemeService {

    String RENDER_TEMPLATE = "themes/%s/%s";

    String THEMES_CACHE_KEY = "themes";

    @Deprecated
    String[] THEME_PROPERTY_FILE_NAMES = {"theme.yaml", "theme.yml"};

    String[] SETTINGS_NAMES = {"settings.yaml", "settings.yml"};

    String THEME_FOLDER = "templates/themes";

    String CUSTOM_SHEET_PREFIX = "sheet_";

    String CUSTOM_POST_PREFIX = "post_";

    String THEME_PROVIDER_REMOTE_NAME = "origin";

    String DEFAULT_REMOTE_BRANCH = "master";

    String RENDER_TEMPLATE_SUFFIX = "themes/%s/%s.ftl";

    /**
     * The type of file that can be modified.
     */
    String[] CAN_EDIT_SUFFIX = {".ftl", ".css", ".js", ".yaml", ".yml", ".properties"};

    String ZIP_FILE_KEY = "zipball_url";

    String TAG_KEY = "tag_name";

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

    @NonNull
    ThemeProperty activateTheme(@NonNull String themeId);

    @NonNull
    List<Group> fetchConfig(@NonNull String themeId);

    void deleteTheme(@NonNull String themeId, @NonNull Boolean deleteSettings);

    @NonNull
    ThemeProperty upload(@NonNull MultipartFile file);

    @NonNull
    ThemeProperty add(@NonNull Path themeTmpPath) throws IOException;

    @NonNull
    ThemeProperty update(@NonNull String themeId);

    ThemeProperty update(@NonNull String themeId, @NonNull MultipartFile file);

    @NonNull
    ThemeProperty fetch(@NonNull String uri);

    @NonNull
    List<ThemeProperty> fetchBranches(@NonNull String uri);

    @NonNull
    List<ThemeProperty> fetchReleases(@NonNull String uri);

    @NonNull
    ThemeProperty fetchRelease(@NonNull String uri, @NonNull String tagName);

    @NonNull
    ThemeProperty fetchBranch(@NonNull String uri, @NonNull String branchName);

    @NonNull
    ThemeProperty fetchLatestRelease(@NonNull String uri);

    void reload();

    @NonNull
    String renderWithSuffix(@NonNull String pageName);

    @NonNull
    String render(@NonNull String pageName);

}
