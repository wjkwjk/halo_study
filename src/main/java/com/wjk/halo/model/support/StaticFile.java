package com.wjk.halo.model.support;

import lombok.Data;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/**
 * 树，用树结构来存储文件/文件夹
 */
@Data
@ToString
public class StaticFile implements Comparator<StaticFile> {

    private String id;

    private String name;

    private String path;

    private String relativePath;

    private Boolean isFile;

    private String mimeType;

    private Long createTime;

    private List<StaticFile> children;

    @Override
    public int compare(StaticFile leftFile, StaticFile rightFile) {
        if (leftFile.isFile && !rightFile.isFile){
            return 1;
        }

        if (!leftFile.isFile && rightFile.isFile){
            return -1;
        }

        return leftFile.getName().compareTo(rightFile.getName());
    }
}
