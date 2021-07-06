package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.CommentBlackList;
import com.wjk.halo.model.enums.CommentViolationTypeEnum;
import com.wjk.halo.model.properties.CommentProperties;
import com.wjk.halo.repository.CommentBlackListRepository;
import com.wjk.halo.repository.PostCommentRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.CommentBlackListService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class CommentBlackListServiceImpl extends AbstractCrudService<CommentBlackList, Long> implements CommentBlackListService {

    private final CommentBlackListRepository commentBlackListRepository;
    private final PostCommentRepository postCommentRepository;
    private final OptionService optionService;

    public CommentBlackListServiceImpl(CommentBlackListRepository commentBlackListRepository, PostCommentRepository postCommentRepository, OptionService optionService) {
        super(commentBlackListRepository);
        this.commentBlackListRepository = commentBlackListRepository;
        this.postCommentRepository = postCommentRepository;
        this.optionService = optionService;
    }

    @Override
    public CommentViolationTypeEnum commentsBanStatus(String ipAddress) {
        Optional<CommentBlackList> blackList = commentBlackListRepository.findByIpAddress(ipAddress);
        LocalDateTime now = LocalDateTime.now();
        Date endTime = new Date(DateTimeUtils.toEpochMilli(now));
        Integer banTime = optionService.getByPropertyOrDefault(CommentProperties.COMMENT_BAN_TIME, Integer.class, 10);
    }
}
