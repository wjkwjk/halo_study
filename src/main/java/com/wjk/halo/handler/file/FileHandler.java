package com.wjk.halo.handler.file;

import com.wjk.halo.model.enums.AttachmentType;
import com.wjk.halo.model.support.UploadResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import static com.wjk.halo.model.support.HaloConst.FILE_SEPARATOR;

public interface FileHandler {

    MediaType IMAGE_TYPE = MediaType.valueOf("image/*");

    AttachmentType getAttachmetType();

    void delete(@NonNull String key);

    @NonNull
    UploadResult upload(@NonNull MultipartFile file);


    @NonNull
    static String normalizeDirectory(@NonNull String dir){
        return StringUtils.appendIfMissing(dir, FILE_SEPARATOR);
    }

    static boolean isImageType(@Nullable MediaType mediaType){
        return mediaType != null && IMAGE_TYPE.includes(mediaType);
    }

}
