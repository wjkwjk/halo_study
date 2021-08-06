package com.wjk.halo.handler.theme.config;

import com.wjk.halo.handler.theme.config.support.Group;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

public interface ThemeConfigResolver {

    @NonNull
    List<Group> resolve(@NonNull String content) throws IOException;

}
