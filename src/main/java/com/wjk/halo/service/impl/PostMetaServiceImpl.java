package com.wjk.halo.service.impl;

import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.entity.PostMeta;
import com.wjk.halo.repository.PostRepository;
import com.wjk.halo.repository.base.BaseMetaRepository;
import com.wjk.halo.service.PostMetaService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostMetaServiceImpl extends BaseMetaServiceImpl<PostMeta> implements PostMetaService {
    private final PostRepository postRepository;

    public PostMetaServiceImpl(BaseMetaRepository<PostMeta> baseMetaRepository, PostRepository postRepository) {
        super(baseMetaRepository);
        this.postRepository = postRepository;
    }

    @Override
    public void validateTarget(@NotNull Integer postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(postId));
    }
}
