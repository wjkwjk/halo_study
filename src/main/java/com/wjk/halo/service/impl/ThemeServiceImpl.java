package com.wjk.halo.service.impl;

import com.wjk.halo.cache.AbstractStringCacheStore;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import com.wjk.halo.model.properties.PrimaryProperties;
import com.wjk.halo.model.support.HaloConst;
import com.wjk.halo.model.support.ThemeFile;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.ThemeService;
import com.wjk.halo.theme.ThemeFileScanner;
import com.wjk.halo.theme.ThemePropertyScanner;
import com.wjk.halo.utils.FileUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wjk.halo.model.support.HaloConst.DEFAULT_THEME_ID;

@Slf4j
@Service
public class ThemeServiceImpl implements ThemeService {

    private final Path themeWorkDir;

    private final OptionService optionService;

    private final AbstractStringCacheStore cacheStore;

    @Nullable
    private volatile String activatedThemeId;

    private volatile ThemeProperty activatedTheme;

    public ThemeServiceImpl(HaloProperties haloProperties,
                            OptionService optionService,
                            AbstractStringCacheStore cacheStore) {
        this.optionService = optionService;
        this.cacheStore = cacheStore;

        themeWorkDir = Paths.get(haloProperties.getWorkDir(), THEME_FOLDER);
    }

    @Override
    public boolean templateExists(String template) {
        if (StringUtils.isBlank(template)){
            return false;
        }
        return fetchActivatedTheme().map(themeProperty -> {
            Path templatePath = Paths.get(themeProperty.getThemePath(), template);

            checkDirectory(templatePath.toString());

            return Files.exists(templatePath);
        }).orElse(false);
    }

    private void checkDirectory(@NonNull String absoluteName){
        ThemeProperty activeThemeProperty = getThemeOfNonNullBy(getActivatedThemeId());
        FileUtils.checkDirectoryTraversal(activeThemeProperty.getThemePath(), absoluteName);
    }

    private void checkDirectory(@NonNull String themeId, @NonNull String absoluteName){
        ThemeProperty themeProperty = getThemeOfNonNullBy(themeId);
        FileUtils.checkDirectoryTraversal(themeProperty.getThemePath(), absoluteName);
    }


    @Override
    public Optional<ThemeProperty> fetchActivatedTheme() {
        return fetchThemePropertyBy(getActivatedThemeId());
    }

    @Override
    @NonNull
    public Optional<ThemeProperty> fetchThemePropertyBy(String themeId) {
        if (StringUtils.isBlank(themeId)){
            return Optional.empty();
        }
        List<ThemeProperty> themes = getThemes();

        return themes.stream()
                .filter(themeProperty -> StringUtils.equals(themeProperty.getId(), themeId))
                .findFirst();
    }

    @Override
    @NonNull
    public List<ThemeProperty> getThemes() {
        ThemeProperty[] themeProperties = cacheStore.getAny(THEMES_CACHE_KEY, ThemeProperty[].class).orElseGet(() -> {
            List<ThemeProperty> properties = ThemePropertyScanner.INSTANCE.scan(getBasePath(), getActivatedThemeId());
            cacheStore.putAny(THEMES_CACHE_KEY, properties);
            return properties.toArray(new ThemeProperty[0]);
        });
        return Arrays.asList(themeProperties);
    }

    @Override
    public Path getBasePath() {
        return themeWorkDir;
    }

    @Override
    public String getActivatedThemeId() {
        if (activatedThemeId == null){
            synchronized (this){
                if (activatedThemeId == null){
                    activatedThemeId = optionService.getByPropertyOrDefault(PrimaryProperties.THEME, String.class, DEFAULT_THEME_ID);
                }
            }
        }
        return activatedThemeId;
    }

    @Override
    public ThemeProperty getThemeOfNonNullBy(String themeId) {
        return fetchThemePropertyBy(themeId).orElseThrow(() -> new NotFoundException(themeId + " 主题不存在或已删除！").setErrorData(themeId));
    }

    @Override
    @NonNull
    public ThemeProperty getActivatedTheme() {
        if (activatedTheme == null){
            synchronized (this){
                if (activatedTheme == null){
                    activatedTheme = getThemeOfNonNullBy(getActivatedThemeId());
                }
            }
        }
        return activatedTheme;
    }

    @Override
    @NonNull
    public List<ThemeFile> listThemeFolderBy(@NonNull String themeId) {
        return fetchThemePropertyBy(themeId)
                .map(themeProperty -> ThemeFileScanner.INSTANCE.scan(themeProperty.getThemePath()))
                .orElse(Collections.emptyList());
    }

    @Override
    public String getTemplateContent(String absolutePath) {
        checkDirectory(absolutePath);

        Path path = Paths.get(absolutePath);

        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        }catch (IOException e){
            throw new ServiceException("读取模板内容失败 " + absolutePath, e);
        }
    }

    @Override
    @NonNull
    public String getTemplateContent(@NonNull String themeId, @NonNull String absolutePath) {
        checkDirectory(themeId, absolutePath);

        Path path = Paths.get(absolutePath);

        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        }catch (IOException e){
            throw new ServiceException("读取模板内容失败 " + absolutePath, e);
        }
    }

    @Override
    public void saveTemplateContent(String absolutePath, String content) {
        // Check the path
        checkDirectory(absolutePath);

        // Write file
        Path path = Paths.get(absolutePath);
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ServiceException("保存模板内容失败 " + absolutePath, e);
        }
    }

    @Override
    public void saveTemplateContent(String themeId, String absolutePath, String content) {
        // Check the path
        checkDirectory(themeId, absolutePath);

        // Write file
        Path path = Paths.get(absolutePath);
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ServiceException("保存模板内容失败 " + absolutePath, e);
        }
    }

    @Override
    @NonNull
    public List<String> listCustomTemplates(String themeId) {
        return listCustomTemplates(themeId, CUSTOM_SHEET_PREFIX);
    }

    @Override
    @NonNull
    public List<String> listCustomTemplates(String themeId, String prefix) {
        return fetchThemePropertyBy(themeId).map(themeProperty -> {
            Path themePath = Paths.get(themeProperty.getThemePath());
            try (Stream<Path> pathStream = Files.list(themePath)){
                return pathStream.filter(path -> StringUtils.startsWithIgnoreCase(path.getFileName().toString(), prefix))
                    .map(path -> {
                        String customTemplate = StringUtils.removeStartIgnoreCase(path.getFileName().toString(), prefix);
                        return StringUtils.removeEndIgnoreCase(customTemplate, HaloConst.SUFFIX_FTL);
                    })
                        .distinct()
                        .collect(Collectors.toList());
            }catch (Exception e){
                throw new ServiceException("Failed to list files of path " + themePath, e);
            }
        }).orElse(Collections.emptyList());
    }




}
