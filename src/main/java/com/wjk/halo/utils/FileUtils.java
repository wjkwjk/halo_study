package com.wjk.halo.utils;

import com.wjk.halo.exception.ForbiddenException;
import com.wjk.halo.model.entity.Link;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class FileUtils {

    private static final List<String> IGNORED_FOLDERS = Arrays.asList(".git");

    private FileUtils() {
    }

    public static boolean isEmpty(@NonNull Path path) throws IOException{
        if (!Files.isDirectory(path) || Files.notExists(path)){
            return true;
        }
        try (Stream<Path> pathStream = Files.list(path)){
            return pathStream.count() == 0;
        }
    }

    public static void deleteFolder(@NonNull Path deletingPath) throws IOException{
        if (Files.notExists(deletingPath)){
            return;
        }
        log.info("Deleting [{}]", deletingPath);

        org.eclipse.jgit.util.FileUtils.delete(deletingPath.toFile(),
                org.eclipse.jgit.util.FileUtils.RECURSIVE | org.eclipse.jgit.util.FileUtils.RETRY);
        log.info("Deleted [{}] successfully", deletingPath);
    }

    public static void copyFolder(@NonNull Path source, @NonNull Path target) throws IOException{
        Files.walkFileTree(source, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path current = target.resolve(source.relativize(dir).toString());
                Files.createDirectories(current);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void createIfAbsent(@NonNull Path path) throws IOException{
        if (Files.notExists(path)){
            Files.createDirectories(path);

            log.debug("Created directory: [{}]", path);
        }
    }

    @NonNull
    public static Path createTempDirectory() throws IOException{
        return Files.createTempDirectory("halo");
    }

    public static void ensureEmpty(@NonNull Path path) throws IOException{
        if (!isEmpty(path)){
            throw new DirectoryNotEmptyException("Target directory: " + path + " was not empty");
        }
    }

    public static void checkDirectoryTraversal(@NonNull String parentPath, @NonNull String pathToCheck){
        checkDirectoryTraversal(Paths.get(parentPath), Paths.get(pathToCheck));
    }

    public static void checkDirectoryTraversal(@NonNull Path parentPath, @NonNull Path pathToCheck){
        if (pathToCheck.normalize().startsWith(parentPath)){
            return;
        }
        throw new ForbiddenException("你没有权限访问 " + pathToCheck).setErrorData(pathToCheck);
    }

    public static void unzip(@NonNull byte[] bytes, @NonNull Path targetPath) throws IOException{
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
        unzip(zis, targetPath);
    }

    public static void unzip(@NonNull ZipInputStream zis, @NonNull Path targetPath) throws IOException{
        createIfAbsent(targetPath);

        ensureEmpty(targetPath);

        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null){
            Path entryPath = targetPath.resolve(zipEntry.getName());

            checkDirectoryTraversal(targetPath, entryPath);

            if (zipEntry.isDirectory()){
                Files.createDirectories(entryPath);
            }else {
                Files.copy(zis, entryPath);
            }
            zipEntry = zis.getNextEntry();
        }
    }

    @NonNull
    public static Optional<Path> findRootPath(@NonNull final Path path, @Nullable final Predicate<Path> pathPredicate) throws IOException{
        if (!Files.isDirectory(path) || pathPredicate == null){
            return Optional.empty();
        }

        log.debug("Trying to find root path from [{}]", path);

        final LinkedList<Path> queue = new LinkedList<>();
        queue.push(path);
        while (!queue.isEmpty()){
            final Path rootPath = queue.pop();
            try (final Stream<Path> childrenPaths = Files.list(rootPath)){
                List<Path> subFolders = new LinkedList<>();
                Optional<Path> matchedPath = childrenPaths.peek(child -> {
                    if (Files.isDirectory(child)){
                        subFolders.add(child);
                    }
                }).filter(pathPredicate).findAny();
                if (matchedPath.isPresent()){
                    log.debug("Found root path: [{}]", rootPath);
                    return Optional.of(rootPath);
                }
                subFolders.forEach(e -> {
                    if (!IGNORED_FOLDERS.contains(e.getFileName().toString())){
                        queue.push(e);
                    }
                });
            }
        }
        return Optional.empty();
    }

    public static void rename(@NonNull Path pathToRename, @NonNull String newName) throws IOException{
        Path newPath = pathToRename.resolveSibling(newName);
        log.info("Rename [{}] to [{}]", pathToRename, newPath);

        Files.move(pathToRename, newPath);

        log.info("Rename [{}] successfully", pathToRename);
    }

    public static void closeQuietly(@Nullable ZipInputStream zipInputStream) {
        try {
            if (zipInputStream != null) {
                zipInputStream.closeEntry();
                zipInputStream.close();
            }
        } catch (IOException e) {
            // Ignore this exception
            log.warn("Failed to close zip input stream", e);
        }
    }

    public static void deleteFolderQuietly(@Nullable Path deletingPath) {
        try {
            if (deletingPath != null) {
                FileUtils.deleteFolder(deletingPath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete " + deletingPath);
        }
    }

}
