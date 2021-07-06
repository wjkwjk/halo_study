package com.wjk.halo.service;

import com.wjk.halo.model.entity.PostCategory;
import com.wjk.halo.service.base.CrudService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface PostCategoryService extends CrudService<PostCategory, Integer> {

    @NonNull
    @Transactional
    List<PostCategory> removeByPostId(@NonNull Integer postId);

    @NonNull
    List<PostCategory> mergeOrCreateByIfAbsent(@NonNull Integer postId, @Nullable Set<Integer> categoryIds);

}
