package com.wjk.halo.theme;

import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.model.support.ThemeFile;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.wjk.halo.service.ThemeService.CAN_EDIT_SUFFIX;

public enum ThemeFileScanner {
    INSTANCE;

    @NonNull
    private List<ThemeFile> scan(@NonNull Path rootPath){
        if (!Files.isDirectory(rootPath)){
            return Collections.emptyList();
        }

        try(Stream<Path> pathStream = Files.list(rootPath)) {
            List<ThemeFile> themeFiles = new LinkedList<>();

            pathStream.forEach(path -> {
                ThemeFile themeFile = new ThemeFile();
                themeFile.setName(path.getFileName().toString());
                themeFile.setPath(path.toString());
                themeFile.setIsFile(Files.isRegularFile(path));
                themeFile.setEditable(isEditable(path));

                if (Files.isDirectory(path)){
                    themeFile.setNode(scan(path));
                }

                themeFiles.add(themeFile);

            });

            themeFiles.sort(new ThemeFile());

            return themeFiles;
        }catch (IOException e){
            throw new ServiceException("Failed to list sub files", e);
        }
    }

    @NonNull
    public List<ThemeFile> scan(@NonNull String absolutePath){
        return scan(Paths.get(absolutePath));
    }

    private boolean isEditable(@NonNull Path path){
        boolean isEditable = Files.isReadable(path) && Files.isWritable(path);

        if (!isEditable){
            return false;
        }

        for (String suffix : CAN_EDIT_SUFFIX){
            if (path.toString().endsWith(suffix)){
                return true;
            }
        }
        return false;
    }

}
