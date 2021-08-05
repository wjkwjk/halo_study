package com.wjk.halo.utils;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FilenameUtils {

    private FilenameUtils(){}

    @NonNull
    public static String getBasename(@NonNull String filename){
        int separatorLastIndex = StringUtils.lastIndexOf(filename, File.separatorChar);

        if (separatorLastIndex == filename.length() - 1){
            return StringUtils.EMPTY;
        }

        if (separatorLastIndex >= 0 && separatorLastIndex < filename.length() - 1){
            filename = filename.substring(separatorLastIndex + 1);
        }

        int doLastIndex = StringUtils.lastIndexOf(filename, '.');

        String[] split = filename.split("\\.");

        List<String> extList = Arrays.asList("gz", "bz2");

        if (extList.contains(split[split.length - 1]) && split.length >= 3){
            return filename.substring(0, filename.substring(0, doLastIndex).lastIndexOf('.'));
        }

        if (doLastIndex < 0){
            return filename;
        }

        return filename.substring(0, doLastIndex);
    }

    @NonNull
    public static String getExtension(@NonNull String filename){
        int separatorLastIndex = StringUtils.lastIndexOf(filename, File.separatorChar);

        if (separatorLastIndex == filename.length() - 1){
            return StringUtils.EMPTY;
        }

        if (separatorLastIndex >= 0 && separatorLastIndex < filename.length() - 1){
            filename = filename.substring(separatorLastIndex + 1);
        }

        int dotLastIndex = StringUtils.lastIndexOf(filename, '.');

        if (dotLastIndex < 0){
            return StringUtils.EMPTY;
        }

        String[] split = filename.split("\\.");

        List<String> extList = Arrays.asList("gz", "bz2");

        if (extList.contains(split[split.length - 1]) && split.length >= 3){
            return filename.substring(filename.substring(0, dotLastIndex).lastIndexOf('.') + 1);
        }

        return filename.substring(dotLastIndex + 1);

    }

}
