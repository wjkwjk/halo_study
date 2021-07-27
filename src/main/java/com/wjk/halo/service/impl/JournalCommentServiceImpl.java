package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.JournalComment;
import com.wjk.halo.repository.JournalCommentRepository;
import com.wjk.halo.repository.JournalRepository;
import com.wjk.halo.repository.base.BaseCommentRepository;
import com.wjk.halo.service.JournalCommentService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class JournalCommentServiceImpl extends BaseCommentServiceImpl<JournalComment> implements JournalCommentService {

    private final JournalCommentRepository journalCommentRepository;

    private final JournalRepository journalRepository;

    public JournalCommentServiceImpl(JournalCommentRepository journalCommentRepository,
                                     OptionService optionService,
                                     UserService userService,
                                     ApplicationEventPublisher eventPublisher,
                                     JournalRepository journalRepository) {
        super(journalCommentRepository, optionService, userService, eventPublisher);
        this.journalCommentRepository = journalCommentRepository;
        this.journalRepository = journalRepository;
    }
}
