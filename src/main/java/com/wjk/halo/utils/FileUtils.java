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

    /**
     * 删除文件/文件夹
     * @param deletingPath
     * @throws IOException
     */
    public static void deleteFolder(@NonNull Path deletingPath) throws IOException{
        if (Files.notExists(deletingPath)){
            return;
        }
        log.info("Deleting [{}]", deletingPath);

        org.eclipse.jgit.util.FileUtils.delete(deletingPath.toFile(),
                org.eclipse.jgit.util.FileUtils.RECURSIVE | org.eclipse.jgit.util.FileUtils.RETRY);
        log.info("Deleted [{}] successfully", deletingPath);
    }

    /**
     * 复制文件/文件夹
     * Files.walkFileTree:遍历目录文件，参数为：
     *      java.nio.file.Path start 遍历的起始路径
     *      Set<java.nio.file.FileVisitOption> options 遍历选项
     *      int maxDepth 遍历深度
     *      java.nio.file.FileVisitor < ? super Path > visitor 遍历过程中的行为控制器
     */
    /**
     * 遍历行为控制器FileVisitor
     * 接口java.nio.file.FileVisitor包含四个方法，涉及到遍历过程中的几个重要的步骤节点。一般实际中使用SimpleFileVisitor简化操作
            public interface FileVisitor<T> {
                FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException;
                FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException;
                FileVisitResult visitFileFailed(T file, IOException exc) throws IOException;
                FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException;
            }
         preVisitDirectory 访问一个目录，在进入之前调用。
         postVisitDirectory 一个目录的所有节点都被访问后调用。遍历时跳过同级目录或有错误发生，Exception会传递给这个方法
         visitFile 文件被访问时被调用。该文件的文件属性被传递给这个方法
         visitFileFailed 当文件不能被访问时，此方法被调用。Exception被传递给这个方法。

     遍历行为结果：
         CONTINUE 继续遍历
         SKIP_SIBLINGS 继续遍历，但忽略当前节点的所有兄弟节点直接返回上一层继续遍历
         SKIP_SUBTREE 继续遍历，但是忽略子目录，但是子文件还是会访问
         TERMINATE 终止遍历

     */
    public static void copyFolder(@NonNull Path source, @NonNull Path target) throws IOException{
        //遍历某个目录下的所有目录
        Files.walkFileTree(source, new SimpleFileVisitor<Path>(){
            //进入到某个目录前调用
            //用于创建目录
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                //source.relativize(dir):返回dir相对于source的相对目录
                Path current = target.resolve(source.relativize(dir).toString());
                Files.createDirectories(current);
                return FileVisitResult.CONTINUE;
            }

            //文件被访问时调用
            //attrs表示文件的属性
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * 创建目录
     * @param path
     * @throws IOException
     */
    public static void createIfAbsent(@NonNull Path path) throws IOException{
        if (Files.notExists(path)){
            Files.createDirectories(path);

            log.debug("Created directory: [{}]", path);
        }
    }

    /**
     * 创建一个临时目录halo,但是该目录不回自动删除，需要手动删除
     * @return
     * @throws IOException
     */
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

    /**
     * 解压
     * @param bytes
     * @param targetPath
     * @throws IOException
     */
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

    /**
     * 以path为根目录，遍历所有文件，判断是否存在满足pathPredicate的文件
     * @param path
     * @param pathPredicate
     * @return
     * @throws IOException
     */
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
