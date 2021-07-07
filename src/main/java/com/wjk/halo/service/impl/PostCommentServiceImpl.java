package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.Option;
import com.wjk.halo.model.entity.PostComment;
import com.wjk.halo.repository.PostCommentRepository;
import com.wjk.halo.repository.PostRepository;
import com.wjk.halo.repository.base.BaseCommentRepository;
import com.wjk.halo.service.CommentBlackListService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostCommentService;
import com.wjk.halo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostCommentServiceImpl extends BaseCommentServiceImpl<PostComment> implements PostCommentService {

    private final PostRepository postRepository;
    private final CommentBlackListService commentBlackListService;

    public PostCommentServiceImpl(PostCommentRepository postCommentRepository,
                                  PostRepository postRepository,
                                  UserService userService,
                                  OptionService optionService,
                                  CommentBlackListService commentBlackListService,
                                  ApplicationEventPublisher eventPublisher) {
        super(postCommentRepository, optionService, userService, eventPublisher);
        this.postRepository = postRepository;
        this.commentBlackListService = commentBlackListService;
    }



}
