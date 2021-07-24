package com.wjk.halo.repository;

import com.wjk.halo.model.entity.PostTag;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface PostTagRepository extends BaseRepository<PostTag, Integer> {

    @NonNull
    List<PostTag> deleteByTagId(@NonNull Integer tagId);

    @NonNull
    List<PostTag> deleteByPostId(@NonNull Integer postId);

    @NonNull
    List<PostTag> findAllByPostId(@NonNull Integer postId);

}
