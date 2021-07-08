package com.wjk.halo.repository.base;

import com.wjk.halo.annotation.SensitiveConceal;
import com.wjk.halo.model.entity.BaseComment;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.projection.CommentCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

@NoRepositoryBean
public interface BaseCommentRepository<COMMENT extends BaseComment> extends BaseRepository<COMMENT, Long>, JpaSpecificationExecutor<COMMENT> {

    long countByPostId(@NonNull Integer postId);

    @NonNull
    List<CommentCountProjection> countByPostIds(@NonNull Collection<Integer> postIds);

    @NonNull
    @SensitiveConceal
    List<COMMENT> findAllByPostId(@NonNull Integer postId);

    @NonNull
    @SensitiveConceal
    Page<COMMENT> findAllByStatus(@Nullable CommentStatus status, @NonNull Pageable pageable);

}
