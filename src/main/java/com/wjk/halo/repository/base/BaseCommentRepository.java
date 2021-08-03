package com.wjk.halo.repository.base;

import com.wjk.halo.annotation.SensitiveConceal;
import com.wjk.halo.model.entity.BaseComment;
import com.wjk.halo.model.enums.CommentStatus;
import com.wjk.halo.model.projection.CommentChildrenCountProjection;
import com.wjk.halo.model.projection.CommentCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

@NoRepositoryBean
public interface BaseCommentRepository<COMMENT extends BaseComment> extends BaseRepository<COMMENT, Long>, JpaSpecificationExecutor<COMMENT> {

    long countByPostId(@NonNull Integer postId);

    @Query("select new com.wjk.halo.model.projection.CommentCountProjection(count(comment.id), comment.postId) " +
            "from BaseComment comment " +
            "where comment.postId in ?1 " +
            "group by comment.postId")
    @NonNull
    List<CommentCountProjection> countByPostIds(@NonNull Collection<Integer> postIds);

    @NonNull
    @SensitiveConceal
    List<COMMENT> findAllByPostId(@NonNull Integer postId);

    @NonNull
    @SensitiveConceal
    Page<COMMENT> findAllByStatus(@Nullable CommentStatus status, @NonNull Pageable pageable);

    @Query("select new com.wjk.halo.model.projection.CommentChildrenCountProjection(count(comment.id), comment.parentId) " +
            "from BaseComment comment " +
            "where comment.parentId in ?1 " +
            "group by comment.parentId")
    @NonNull
    List<CommentChildrenCountProjection> findDirectChildrenCount(@NonNull Collection<Long> commentIds);


    @NonNull
    @SensitiveConceal
    Page<COMMENT> findAllByPostIdAndStatus(Integer postId, CommentStatus status, Pageable pageable);

    @NonNull
    @SensitiveConceal
    List<COMMENT> findAllByPostIdAndParentId(@NonNull Integer postId, @NonNull Long parentId);

    @SensitiveConceal
    List<COMMENT> findAllByParentIdIn(@NonNull Collection<Long> parentIds);
}
