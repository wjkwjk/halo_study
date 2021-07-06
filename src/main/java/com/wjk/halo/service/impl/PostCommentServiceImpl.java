package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.PostComment;
import com.wjk.halo.repository.PostRepository;
import com.wjk.halo.service.PostCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostCommentServiceImpl extends BaseCommentServiceImpl<PostComment> implements PostCommentService {

    private final PostRepository postRepository;
    private final Comme

}
