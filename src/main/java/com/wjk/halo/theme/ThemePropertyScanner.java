package com.wjk.halo.theme;

import com.wjk.halo.handler.theme.config.ThemePropertyResolver;
import com.wjk.halo.handler.theme.config.impl.YamlThemePropertyResolver;
import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import com.wjk.halo.utils.FilenameUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wjk.halo.service.ThemeService.SETTINGS_NAMES;

@Slf4j
public enum ThemePropertyScanner {
    INSTANCE;

    private static final String[] THEME_PROPERTY_FILE_NAMES = {"theme.yaml", "theme.yml"};

    private static final String THEME_SCREENSHOTS_NAME = "screenshot";
    private final ThemePropertyResolver propertyResolver = new YamlThemePropertyResolver();

    @NonNull
    public List<ThemeProperty> scan(@NonNull Path themePath, @Nullable String activeThemeId){
        try {
            if (Files.notExists(themePath)){
                Files.createDirectories(themePath);
            }
        }catch (IOException e){
            log.error("Failed to create directory: " + themePath, e);
            return Collections.emptyList();
        }
        try (Stream<Path> pathStream = Files.list(themePath)){
            List<Path> themePaths = pathStream.filter(Files::isDirectory)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(themePaths)){
                return Collections.emptyList();
            }

            ThemeProperty[] properties = themePaths.stream()
                    .map(this::fetchThemeProperty)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .peek(themeProperty -> {
                        if (StringUtils.equals(activeThemeId, themeProperty.getId())){
                            themeProperty.setActivated(true);
                        }
                    }).toArray(ThemeProperty[]::new);
            return Arrays.asList(properties);
        }catch (IOException e){
            log.error("Failed to get themes", e);
            return Collections.emptyList();
        }
    }


    @NonNull
    public Optional<ThemeProperty> fetchThemeProperty(@NonNull Path themePath){
        Optional<Path> optionalPath = fetchPropertyPath(themePath);

        if (!optionalPath.isPresent()){
            return Optional.empty();
        }

        Path propertyPath = optionalPath.get();

        try {
            String propertyContent = new String(Files.readAllBytes(propertyPath), StandardCharsets.UTF_8);

            ThemeProperty themeProperty = propertyResolver.resolve(propertyContent);

            themeProperty.setThemePath(themePath.toString());
            themeProperty.setFolderName(themePath.getFileName().toString());
            themeProperty.setHasOptions(hasOptions(themePath));
            themeProperty.setActivated(false);

            getScreenshotsFileName(themePath).ifPresent(screenshotsName -> themeProperty.setScreenshots(StringUtils.join("/themes/",
                    FilenameUtils.getBasename(themeProperty.getThemePath()),
                    "/",
                    screenshotsName)));
            return Optional.of(themeProperty);
        }catch (Exception e){
            log.warn("Failed to load theme property file", e);
        }
        return Optional.empty();
    }

    @NonNull
    private Optional<String> getScreenshotsFileName(@NonNull Path themePath) throws IOException{
        try (Stream<Path> pathStream = Files.list(themePath)){
            return pathStream.filter(path -> Files.isRegularFile(path)
            && Files.isReadable(path)
            && FilenameUtils.getBasename(path.toString()).equalsIgnoreCase(THEME_SCREENSHOTS_NAME))
            .findFirst()
            .map(path -> path.getFileName().toString());
        }
    }

    private boolean hasOptions(@NonNull Path themePath){
        for (String optionsName : SETTINGS_NAMES){
            Path optionsPath = themePath.resolve(optionsName);

            log.debug("Check options file for path: [{}]", optionsPath);

            if (Files.exists(optionsPath)){
                return true;
            }
        }
        return false;
    }

    @NonNull
    private Optional<Path> fetchPropertyPath(@NonNull Path themePath){
        for (String propertyPathName : THEME_PROPERTY_FILE_NAMES){
            Path propertyPath = themePath.resolve(propertyPathName);

            log.debug("Attempting to find property file: [{}]", propertyPath);
            if (Files.exists(propertyPath) && Files.isReadable(propertyPath)){
                log.debug("Found property file: [{}]", propertyPath);
                return Optional.of(propertyPath);
            }
        }
        log.warn("Property file was not found in [{}]", themePath);
        return Optional.empty();
    }
}
