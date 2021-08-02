package com.wjk.halo.handler.theme.config.impl;

import com.wjk.halo.handler.theme.config.ThemePropertyResolver;
import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import com.wjk.halo.theme.YamlResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;

@Service
public class YamlThemePropertyResolver implements ThemePropertyResolver {
    @Override
    @NonNull
    public ThemeProperty resolve(@NonNull String content) throws IOException {
        Assert.hasText(content, "Theme file content must not be null");

        return YamlResolver.INSTANCE.getYamlMapper().readValue(content, ThemeProperty.class);
    }
}
