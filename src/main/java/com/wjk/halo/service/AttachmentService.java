package com.wjk.halo.service;

import com.wjk.halo.model.dto.AttachmentDTO;
import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.model.enums.AttachmentType;
import com.wjk.halo.model.params.AttachmentQuery;
import com.wjk.halo.service.base.CrudService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface AttachmentService extends CrudService<Attachment, Integer> {

    @NonNull
    Page<AttachmentDTO> pageDtosBy(@NonNull Pageable pageable, AttachmentQuery attachmentQuery);

    @NonNull
    AttachmentDTO convertToDto(@NonNull Attachment attachment);

    @NonNull
    Attachment removePermanently(@NonNull Integer id);

    @NonNull
    List<Attachment> removePermanently(@NonNull Collection<Integer> ids);

    @NonNull
    Attachment upload(@NonNull MultipartFile file);

    List<String> listAllMediaType();

    List<AttachmentType> listAllType();
}
