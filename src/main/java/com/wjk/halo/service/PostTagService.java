package com.wjk.halo.service;

import com.wjk.halo.model.entity.PostTag;
import com.wjk.halo.service.base.CrudService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface PostTagService extends CrudService<PostTag, Integer> {

    @NonNull
    @Transactional
    List<PostTag> removeByTagId(@NonNull Integer tagId);

    @NonNull
    @Transactional
    List<PostTag> removeByPostId(@NonNull Integer postId);

    @NonNull
    List<PostTag> mergeOrCreateByIfAbsent(@NonNull Integer postId, @Nullable Set<Integer> tagIds);

}
