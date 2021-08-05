package com.wjk.halo.service;

import com.wjk.halo.model.dto.AttachmentDTO;
import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.model.params.AttachmentQuery;
import com.wjk.halo.service.base.CrudService;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttachmentService extends CrudService<Attachment, Integer> {

    @NonNull
    Page<AttachmentDTO> pageDtosBy(@NonNull Pageable pageable, AttachmentQuery attachmentQuery);

    @NonNull
    AttachmentDTO convertToDto(@NonNull Attachment attachment);

}
