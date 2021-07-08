package com.wjk.halo.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Locale;

public class SlugUtils {

    public static String slug(@NonNull String input) {
        Assert.hasText(input, "Input string must not be blank");
        String slug = input.
                replaceAll("[^(a-zA-Z0-9\\u4e00-\\u9fa5\\.\\-)]", "").
                replaceAll("[\\?\\\\/:|<>\\*\\[\\]\\(\\)\\$%\\{\\}@~\\.]", "").
                replaceAll("\\s", "")
                .toLowerCase(Locale.ENGLISH);
        return StringUtils.isNotEmpty(slug) ? slug : String.valueOf(System.currentTimeMillis());
    }

}
