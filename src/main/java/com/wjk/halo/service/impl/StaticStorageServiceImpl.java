package com.wjk.halo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.event.StaticStorageChangedEvent;
import com.wjk.halo.exception.FileOperationException;
import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.model.support.StaticFile;
import com.wjk.halo.service.StaticStorageService;
import com.wjk.halo.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class StaticStorageServiceImpl implements StaticStorageService, ApplicationListener<ApplicationStartedEvent> {

    private final Path staticDir;

    private final ApplicationEventPublisher eventPublisher;

    public StaticStorageServiceImpl(HaloProperties haloProperties,
                                    ApplicationEventPublisher eventPublisher) throws IOException {
        //staticDir: 用户根目录/.halo/static
        staticDir = Paths.get(haloProperties.getWorkDir(), STATIC_FOLDER);
        this.eventPublisher = eventPublisher;
        FileUtils.createIfAbsent(staticDir);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        onChange();
    }

    /**
     * 发布StaticStorageChangedEvent事件
     */
    private void onChange(){
        eventPublisher.publishEvent(new StaticStorageChangedEvent(this, staticDir));
    }

    /**
     * 用树的结构返回staticDir下的所有目录和文件
     * @return
     */
    @Override
    public List<StaticFile> listStaticFolder() {
        return listStaticFileTree(staticDir);
    }

    @Override
    public void delete(String relativePath) {
        Path path = Paths.get(staticDir.toString(), relativePath);

        FileUtils.checkDirectoryTraversal(staticDir.toString(), path.toString());

        log.debug(path.toString());

        try {
            if (path.toFile().isDirectory()){
                FileUtils.deleteFolder(path);
            }else {
                Files.deleteIfExists(path);
            }
            onChange();
        }catch (IOException e){
            throw new FileOperationException("文件 " + relativePath + " 删除失败", e);
        }

    }

    @Override
    public void createFolder(String basePath, String folderName) {
        Path path;

        if (StringUtils.startsWith(folderName, API_FOLDER_NAME)){
            throw new FileOperationException("目录名称 " + folderName + " 不合法");
        }

        if (StringUtils.isEmpty(basePath)){
            path = Paths.get(staticDir.toString(), folderName);
        }else {
            path = Paths.get(staticDir.toString(), basePath, folderName);
        }

        FileUtils.checkDirectoryTraversal(staticDir.toString(), path.toString());

        if (path.toFile().exists()){
            throw new FileOperationException("目录 " + path.toString() + " 已存在").setErrorData(path);
        }

        try {
            FileUtils.createIfAbsent(path);
        }catch (IOException e){
            throw new FileOperationException("目录 " + path.toString() + " 创建失败", e);
        }

    }

    /**
     * 前端发送了file，用来更新basePath
     * @param basePath
     * @param file
     */
    @Override
    public void upload(String basePath, MultipartFile file) {
        Path uploadPath;
        //判断上传的文件名是否已api开头
        if (StringUtils.startsWith(file.getOriginalFilename(), API_FOLDER_NAME)){
            throw new FileOperationException("文件名称 " + file.getOriginalFilename() + " 不合法");
        }
        //拼接得到用于存放上传的文件的目录
        if (StringUtils.isEmpty(basePath)){
            uploadPath = Paths.get(staticDir.toString(), file.getOriginalFilename());
        }else {
            uploadPath = Paths.get(staticDir.toString(), basePath, file.getOriginalFilename());
        }

        FileUtils.checkDirectoryTraversal(staticDir.toString(), uploadPath.toString());

        if (uploadPath.toFile().exists()) {
            throw new FileOperationException("文件 " + file.getOriginalFilename() + " 已存在").setErrorData(uploadPath);
        }

        //保存用户上传的文件
        try {
            Files.createFile(uploadPath);
            file.transferTo(uploadPath);
            //发送事件
            onChange();
        }catch (IOException e){
            throw new ServiceException("上传文件失败").setErrorData(uploadPath);
        }
    }

    @Override
    public void rename(String basePath, String newName) {
        Path pathToRename;

        if (StringUtils.startsWith(newName, API_FOLDER_NAME)) {
            throw new FileOperationException("重命名名称 " + newName + " 不合法");
        }

        pathToRename = Paths.get(staticDir.toString(), basePath);

        // check if the path is valid (not outside staticDir)
        FileUtils.checkDirectoryTraversal(staticDir.toString(), pathToRename.toString());

        try {
            FileUtils.rename(pathToRename, newName);
            onChange();
        } catch (FileAlreadyExistsException e) {
            throw new FileOperationException("该路径下名称 " + newName + " 已存在");
        } catch (IOException e) {
            throw new FileOperationException("重命名 " + pathToRename.toString() + " 失败");
        }
    }

    @Override
    public void save(String path, String content) {
        Path savePath = Paths.get(staticDir.toString(), path);

        // check if the path is valid (not outside staticDir)
        FileUtils.checkDirectoryTraversal(staticDir.toString(), savePath.toString());

        // check if file exist
        if (!Files.isRegularFile(savePath)) {
            throw new FileOperationException("路径 " + path + " 不合法");
        }

        try {
            Files.write(savePath, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ServiceException("保存内容失败 " + path, e);
        }
    }

    /**
     * 递归遍历 topPath下的文件/文件夹，并以树的形式返回
     * @param topPath
     * @return
     */

    @Nullable
    private List<StaticFile> listStaticFileTree(@NonNull Path topPath){
        if (!Files.isDirectory(topPath)){
            return null;
        }

        //Files.list(topPath)：返回topPath下的一级文件和一级目录
        try(Stream<Path> pathStream = Files.list(topPath)) {
            List<StaticFile> staticFiles = new LinkedList<>();

            pathStream.forEach(path -> {
                StaticFile staticFile = new StaticFile();
                staticFile.setId(IdUtil.fastSimpleUUID());
                staticFile.setName(path.getFileName().toString());
                staticFile.setPath(path.toString());
                staticFile.setRelativePath(StringUtils.removeStart(path.toString(), staticDir.toString()));
                staticFile.setIsFile(Files.isRegularFile(path));
                try {
                    staticFile.setCreateTime(Files.getLastModifiedTime(path).toMillis());
                }catch (IOException e){
                    e.printStackTrace();
                }
                staticFile.setMimeType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(path.toFile()));
                if (Files.isDirectory(path)){
                    /**
                     * 递归，生成文件树
                     */
                    staticFile.setChildren(listStaticFileTree(path));
                }
                staticFiles.add(staticFile);
            });

            staticFiles.sort(new StaticFile());
            return staticFiles;
        }catch (IOException e){
            throw new ServiceException("Failed to list sub files", e);
        }

    }

}
