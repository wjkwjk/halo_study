package com.wjk.halo.controller.admin.api;


import com.wjk.halo.model.dto.AttachmentDTO;
import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.model.enums.AttachmentType;
import com.wjk.halo.model.params.AttachmentParam;
import com.wjk.halo.model.params.AttachmentQuery;
import com.wjk.halo.service.AttachmentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import java.util.LinkedList;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/admin/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public Page<AttachmentDTO> pageBy(@PageableDefault(sort = "createTime", direction = DESC) Pageable pageable,
                                      AttachmentQuery attachmentQuery){
        return attachmentService.pageDtosBy(pageable, attachmentQuery);
    }

    @GetMapping("{id:\\d+}")
    public AttachmentDTO getBy(@PathVariable("id") Integer id){
        Attachment attachment = attachmentService.getById(id);
        return attachmentService.convertToDto(attachment);
    }

    @PutMapping("{attachmentId:\\d+}")
    public AttachmentDTO updateBy(@PathVariable("attachmentId") Integer attachmentId,
                                  @RequestBody @Valid AttachmentParam attachmentParam){
        Attachment attachment = attachmentService.getById(attachmentId);
        attachmentParam.update(attachment);
        return new AttachmentDTO().convertFrom(attachmentService.update(attachment));
    }

    @DeleteMapping("{id:\\d+}")
    public AttachmentDTO deletePermanently(@PathVariable("id") Integer id){
        return attachmentService.convertToDto(attachmentService.removePermanently(id));
    }

    @DeleteMapping
    public List<Attachment> deletePermanentlyInBatch(@RequestBody List<Integer> ids) {
        return attachmentService.removePermanently(ids);
    }

    @PostMapping("upload")
    public AttachmentDTO uploadAttachment(@RequestPart("file") MultipartFile file){
        return attachmentService.convertToDto(attachmentService.upload(file));
    }

    @PostMapping(value = "uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<AttachmentDTO> uploadAttachments(@RequestPart("files") MultipartFile[] files){
        List<AttachmentDTO> result = new LinkedList<>();

        for (MultipartFile file : files){
            Attachment attachment = attachmentService.upload(file);

            result.add(attachmentService.convertToDto(attachment));
        }
        return result;
    }

    @GetMapping("media_types")
    public List<String> listMediaTypes(){
        return attachmentService.listAllMediaType();
    }

    @GetMapping("types")
    public List<AttachmentType> listTypes(){
        return attachmentService.listAllType();
    }

}
