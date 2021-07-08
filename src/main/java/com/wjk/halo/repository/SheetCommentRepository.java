package com.wjk.halo.repository;

import com.wjk.halo.model.entity.SheetComment;
import com.wjk.halo.model.projection.CommentChildrenCountProjection;
import com.wjk.halo.model.projection.CommentCountProjection;
import com.wjk.halo.repository.base.BaseCommentRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

public interface SheetCommentRepository extends BaseCommentRepository<SheetComment> {

    /**
     * Count comments by sheet ids.
     *
     * @param sheetIds sheet id collection must not be null
     * @return a list of CommentCountProjection
     */
    @Query("select new com.wjk.halo.model.projection.CommentCountProjection(count(comment.id), comment.postId) " +
            "from SheetComment comment " +
            "where comment.postId in ?1 group by comment.postId")
    @NonNull
    @Override
    List<CommentCountProjection> countByPostIds(@NonNull Collection<Integer> sheetIds);

    /**
     * Finds direct children count by comment ids.
     *
     * @param commentIds comment ids must not be null.
     * @return a list of CommentChildrenCountProjection
     */
    @Query("select new com.wjk.halo.model.projection.CommentChildrenCountProjection(count(comment.id), comment.parentId) " +
            "from SheetComment comment " +
            "where comment.parentId in ?1 " +
            "group by comment.parentId")
    @NonNull
    @Override
    List<CommentChildrenCountProjection> findDirectChildrenCount(@NonNull Collection<Long> commentIds);

}
