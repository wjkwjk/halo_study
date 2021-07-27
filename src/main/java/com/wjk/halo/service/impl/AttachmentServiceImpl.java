package com.wjk.halo.service.impl;

import com.wjk.halo.handler.file.FileHandlers;
import com.wjk.halo.model.entity.Attachment;
import com.wjk.halo.repository.AttachmentRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.AttachmentService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
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
}
