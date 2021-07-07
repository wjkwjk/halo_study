package com.wjk.halo.service;

import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.entity.PostMeta;
import com.wjk.halo.model.vo.PostDetailVO;
import com.wjk.halo.service.base.BasePostService;
import org.springframework.lang.NonNull;

import java.util.Set;

public interface PostService extends BasePostService<Post> {

    @NonNull
    PostDetailVO createBy(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, boolean autoSave);


    @NonNull
    PostDetailVO createBy(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, Set<PostMeta> metas, boolean autoSave);
}
