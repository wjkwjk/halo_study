package com.wjk.halo.model.params;

import com.wjk.halo.model.enums.AttachmentType;
import lombok.Data;

@Data
public class AttachmentQuery {
    private String keyword;

    private String mediaType;

    private AttachmentType attachmentType;
}
