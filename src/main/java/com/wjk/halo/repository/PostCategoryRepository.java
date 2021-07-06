package com.wjk.halo.repository;

import com.wjk.halo.model.entity.PostCategory;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface PostCategoryRepository extends BaseRepository<PostCategory, Integer> {

    @NonNull
    List<PostCategory> deleteByPostId(@NonNull Integer postId);

    @NonNull
    List<PostCategory> findAllByPostId(@NonNull Integer postId);

}
