package com.wjk.halo.repository;

import com.wjk.halo.model.entity.PostTag;
import com.wjk.halo.model.projection.TagPostPostCountProjection;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PostTagRepository extends BaseRepository<PostTag, Integer> {

    @NonNull
    List<PostTag> deleteByTagId(@NonNull Integer tagId);

    @NonNull
    List<PostTag> deleteByPostId(@NonNull Integer postId);

    @NonNull
    List<PostTag> findAllByPostId(@NonNull Integer postId);

    @NonNull
    List<PostTag> findAllByPostIdIn(@NonNull Collection<Integer> postIds);

    @Query("select postTag.tagId from PostTag postTag where postTag.postId = ?1")
    @NonNull
    Set<Integer> findAllTagIdsByPostId(@NonNull Integer postId);

    @Query("select new com.wjk.halo.model.projection.TagPostPostCountProjection(count(pt.postId), pt.tagId) from PostTag pt group by pt.tagId")
    @NonNull
    List<TagPostPostCountProjection> findPostCount();
}
