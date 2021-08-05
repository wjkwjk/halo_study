package com.wjk.halo.controller.admin.api;


import com.wjk.halo.model.dto.AttachmentDTO;
import com.wjk.halo.model.params.AttachmentQuery;
import com.wjk.halo.service.AttachmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/admin/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public Page<AttachmentDTO> pageBy(@PageableDefault(sort = "createTime", direction = DESC) Pageable pageable,
                                      AttachmentQuery attachmentQuery){
        return attachmentService.
    }

}
