package com.wjk.halo.service;

import com.wjk.halo.model.support.StaticFile;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StaticStorageService {

    String API_FOLDER_NAME = "api";

    /**
     * Static folder location.
     */
    String STATIC_FOLDER = "static";

    List<StaticFile> listStaticFolder();

    void delete(@NonNull String relativePath);

    void createFolder(String basePath, @NonNull String folderName);

    void upload(String basePath, @NonNull MultipartFile file);

    void rename(@NonNull String basePath, @NonNull String newName);

    void save(@NonNull String path, String content);
}
