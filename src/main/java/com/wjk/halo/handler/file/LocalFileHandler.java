package com.wjk.halo.handler.file;

import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.exception.FileOperationException;
import com.wjk.halo.model.enums.AttachmentType;
import com.wjk.halo.model.support.UploadResult;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.utils.FilenameUtils;
import com.wjk.halo.utils.HaloUtils;
import com.wjk.halo.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static com.wjk.halo.model.support.HaloConst.FILE_SEPARATOR;

@Slf4j
@Component
public class LocalFileHandler implements FileHandler{

    /**
     * Upload sub directory.
     */
    private final static String UPLOAD_SUB_DIR = "upload/";

    private final static String THUMBNAIL_SUFFIX = "-thumbnail";

    /**
     * Thumbnail width.
     */
    private final static int THUMB_WIDTH = 256;

    /**
     * Thumbnail height.
     */
    private final static int THUMB_HEIGHT = 256;

    private final OptionService optionService;

    private final String workDir;

    ReentrantLock lock = new ReentrantLock();

    public LocalFileHandler(OptionService optionService, HaloProperties haloProperties) {
        this.optionService = optionService;

        workDir = FileHandler.normalizeDirectory(haloProperties.getWorkDir());

        checkWorkDir();
    }

    private void checkWorkDir(){
        // Get work path
        Path workPath = Paths.get(workDir);

        // Check file type
        Assert.isTrue(Files.isDirectory(workPath), workDir + " isn't a directory");

        // Check readable
        Assert.isTrue(Files.isReadable(workPath), workDir + " isn't readable");

        // Check writable
        Assert.isTrue(Files.isWritable(workPath), workDir + " isn't writable");
    }

    @Override
    public AttachmentType getAttachmetType() {
        return AttachmentType.LOCAL;
    }

    @Override
    public void delete(String key) {
        Path path = Paths.get(workDir, key);

        try {
            Files.deleteIfExists(path);
        }catch (IOException e){
            throw new FileOperationException("附件 " + key + " 删除失败", e);
        }

        // Delete thumb if necessary
        String basename = FilenameUtils.getBasename(key);
        String extension = FilenameUtils.getExtension(key);

        // Get thumbnail name
        String thumbnailName = basename + THUMBNAIL_SUFFIX + '.' + extension;

        // Get thumbnail path
        Path thumbnailPath = Paths.get(path.getParent().toString(), thumbnailName);

        // Delete thumbnail file
        try {
            boolean deleteResult = Files.deleteIfExists(thumbnailPath);
            if (!deleteResult){
                log.warn("Thumbnail: [{}] may not exist", thumbnailPath.toString());
            }
        }catch (IOException e){
            throw new FileOperationException("附件缩略图 " + thumbnailName + " 删除失败", e);
        }

    }

    @Override
    public UploadResult upload(MultipartFile file) {
        Calendar current = Calendar.getInstance(optionService.getLocale());
        //获得上传日期
        int year = current.get(Calendar.YEAR);
        int month = current.get(Calendar.MONTH) + 1;

        String monthString = month < 10 ? "0" + month : String.valueOf(month);
        //新建目录
        // Build directory
        String subDir = UPLOAD_SUB_DIR + year + FILE_SEPARATOR + monthString + FILE_SEPARATOR;
        //获取文件名（不包括文件后缀名）
        String originalBasename = FilenameUtils.getBasename(Objects.requireNonNull(file.getOriginalFilename()));
        //文件名加一个随机字符串，用于防止上传的多个相同名字的文件冲突
        String basename = originalBasename + "-" + HaloUtils.randomUUIDWithoutDash();
        //获取文件后缀名
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        log.debug("Base name: [{}], extension: [{}] of original filename: [{}]", basename, extension, file.getOriginalFilename());
        //在生成的目录下保存当前文件，并且重新生成文件名
        String subFilePath = subDir + basename + '.' + extension;
        //生成绝对路径
        Path uploadPath = Paths.get(workDir, subFilePath);

        log.info("Uploading file: [{}]to directory: [{}]", file.getOriginalFilename(), uploadPath.toString());

        try {
            // TODO Synchronize here
            // Create directory
            Files.createDirectories(uploadPath.getParent());
            Files.createFile(uploadPath);

            // Upload this file
            file.transferTo(uploadPath);

            // Build upload result
            UploadResult uploadResult = new UploadResult();
            uploadResult.setFilename(originalBasename);
            uploadResult.setFilePath(subFilePath);
            uploadResult.setKey(subFilePath);
            uploadResult.setSuffix(extension);
            uploadResult.setMediaType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            uploadResult.setSize(file.getSize());

            // TODO refactor this: if image is svg ext. extension
            boolean isSvg = "svg".equals(extension);
            //判断是否是图片，用来设置缩略图地址，如果不是图片，在缩略图与文件地址相同
            if (FileHandler.isImageType(uploadResult.getMediaType()) && !isSvg){
                lock.lock();
                try (InputStream uploadFileInputStream = new FileInputStream(uploadPath.toFile())){
                    String thumbnailBasename = basename + THUMBNAIL_SUFFIX;
                    String thumbnailSubFilePath = subDir + thumbnailBasename + '.' + extension;
                    Path thumbnailPath = Paths.get(workDir + thumbnailSubFilePath);

                    BufferedImage originalImage = ImageUtils.getImageFromFile(uploadFileInputStream, extension);

                    uploadResult.setWidth(originalImage.getWidth());
                    uploadResult.setHeight(originalImage.getHeight());

                    boolean result = generateThumbnail(originalImage, thumbnailPath, extension);
                    if (result){
                        uploadResult.setThumbPath(thumbnailSubFilePath);
                    }else {
                        uploadResult.setThumbPath(subFilePath);
                    }
                }finally {
                    lock.unlock();
                }
            }else {
                uploadResult.setThumbPath(subFilePath);
            }

            log.info("Uploaded file: [{}] to directory: [{}] successfully", file.getOriginalFilename(), uploadPath.toString());
            return uploadResult;
        }catch (IOException e){
            throw new FileOperationException("上传附件失败").setErrorData(uploadPath);
        }

    }

    private boolean generateThumbnail(BufferedImage originalImage, Path thumbPath, String extension){
        boolean result = false;
        // Create the thumbnail
        try {
            Files.createFile(thumbPath);
            // Convert to thumbnail and copy the thumbnail
            log.debug("Trying to generate thumbnail: [{}]", thumbPath.toString());
            Thumbnails.of(originalImage).size(THUMB_WIDTH, THUMB_HEIGHT).keepAspectRatio(true).toFile(thumbPath.toFile());
            log.debug("Generated thumbnail image, and wrote the thumbnail to [{}]", thumbPath.toString());
            result = true;

        }catch (Throwable t){
            log.warn("Failed to generate thumbnail: " + thumbPath, t);
        }finally {
            // Disposes of this graphics context and releases any system resources that it is using.
            if (originalImage != null) {
                originalImage.getGraphics().dispose();
            }
        }
        return result;
    }

}
