package com.wjk.halo.service.base;

import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import com.wjk.halo.model.dto.post.BasePostSimpleDTO;
import com.wjk.halo.model.entity.BasePost;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface BasePostService<POST extends BasePost> extends CrudService<POST, Integer>{

    long countByStatus(PostStatus Status);

    @NonNull
    POST createOrUpdateBy(@NonNull POST post);

    long countVisit();

    long countLike();

    @NonNull
    Page<BasePostSimpleDTO> convertToSimple(@NonNull Page<POST> postPage);

    @NonNull
    BasePostSimpleDTO convertToSimple(@NonNull POST post);

    @NonNull
    List<BasePostMinimalDTO> convertToMinimal(@Nullable List<POST> posts);

    @NonNull
    BasePostMinimalDTO convertToMinimal(@NonNull POST post);

    @NonNull
    Page<POST> pageLatest(int top);

    @NonNull
    Page<POST> pageBy(@NonNull Pageable pageable);

    @NonNull
    Page<POST> pageBy(@NonNull PostStatus status, @NonNull Pageable pageable);

    void increaseLike(@NonNull Integer postId);

    void increaseLike(long likes, @NonNull Integer postId);

    @NonNull
    POST updateStatus(@NonNull PostStatus status, @NonNull Integer postId);

    @NonNull
    List<POST> updateStatusByIds(@NonNull List<Integer> ids, @NonNull PostStatus status);

    @NonNull
    POST updateDraftContent(@Nullable String content, @NonNull Integer postId);

    void increaseVisit(@NonNull Integer postId);

    void increaseVisit(long visits, @NonNull Integer postId);

    @NonNull
    POST getBy(@NonNull PostStatus status, @NonNull String slug);

    @NonNull
    Optional<POST> getPrevPost(@NonNull POST post);

    @NonNull
    List<POST> listPrevPosts(@NonNull POST post, int size);

    @NonNull
    Optional<POST> getNextPost(@NonNull POST post);

    @NonNull
    List<POST> listNextPosts(@NonNull POST post, int size);

    String generateDescription(@NonNull String content);

    @NonNull
    POST getBySlug(@NonNull String slug);
}
