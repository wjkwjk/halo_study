package com.wjk.halo.handler.theme.config;

import com.wjk.halo.handler.theme.config.support.ThemeProperty;
import org.springframework.lang.NonNull;

import java.io.IOException;

public interface ThemePropertyResolver {
    @NonNull
    ThemeProperty resolve(@NonNull String content) throws IOException;
}
