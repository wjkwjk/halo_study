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

}
