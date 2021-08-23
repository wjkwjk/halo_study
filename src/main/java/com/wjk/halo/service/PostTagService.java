package com.wjk.halo.service;

import com.wjk.halo.model.dto.TagWithPostCountDTO;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.entity.PostTag;
import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    @NonNull
    Map<Integer, List<Tag>> listTagListMapBy(@Nullable Collection<Integer> postIds);

    @NonNull
    List<Tag> listTagsBy(@NonNull Integer postId);

    @NonNull
    List<TagWithPostCountDTO> listTagWithCountDtos(@NonNull Sort sort);

    Page<Post> pagePostsBy(@NonNull Integer tagId, @NonNull PostStatus status, Pageable pageable);

}
