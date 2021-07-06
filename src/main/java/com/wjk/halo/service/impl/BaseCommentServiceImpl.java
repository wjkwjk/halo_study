package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.BaseComment;
import com.wjk.halo.repository.base.BaseCommentRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.UserService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.service.base.BaseCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public abstract class BaseCommentServiceImpl<COMMENT extends BaseComment> extends AbstractCrudService<COMMENT, Long> implements BaseCommentService<COMMENT> {

    protected final OptionService optionService;
    protected final UserService userService;
    protected final ApplicationEventPublisher eventPublisher;
    private final BaseCommentRepository<COMMENT> baseCommentRepository;

    public BaseCommentServiceImpl(BaseCommentRepository<COMMENT> baseCommentRepository, OptionService optionService, UserService userService, ApplicationEventPublisher eventPublisher) {
        super(baseCommentRepository);
        this.optionService = optionService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.baseCommentRepository = baseCommentRepository;
    }

    @Override
    public long countByPostId(Integer postId) {
        return baseCommentRepository.countByPostId(postId);
    }
}
