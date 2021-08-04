package com.wjk.halo.service.impl;

import com.wjk.halo.exception.AlreadyExistsException;
import com.wjk.halo.handler.file.FileHandlers;
import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.repository.AttachmentRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.AttachmentService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.logging.FileHandler;

@Slf4j
@Service
public class AttachmentServiceImpl extends AbstractCrudService<Attachment, Integer> implements AttachmentService {

    private final AttachmentRepository attachmentRepository;

    private OptionService optionService;

    private FileHandlers fileHandlers;


    public AttachmentServiceImpl(AttachmentRepository attachmentRepository, OptionService optionService, FileHandlers fileHandlers) {
        super(attachmentRepository);
        this.attachmentRepository = attachmentRepository;
        this.optionService = optionService;
        this.fileHandlers = fileHandlers;
    }

    @Override
    public Attachment create(Attachment attachment) {
        pathMustNotExist(attachment);
        return super.create(attachment);
    }

    private void pathMustNotExist(@NonNull Attachment attachment){
        long pathCount = attachmentRepository.countByPath(attachment.getPath());

        if (pathCount > 0){
            throw new AlreadyExistsException("附件路径为 " + attachment.getPath() + " 已经存在");
        }
    }

}
