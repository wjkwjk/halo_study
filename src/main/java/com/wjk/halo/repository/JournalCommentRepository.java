package com.wjk.halo.repository;

import com.wjk.halo.model.entity.JournalComment;
import com.wjk.halo.model.projection.CommentCountProjection;
import com.wjk.halo.repository.base.BaseCommentRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

public interface JournalCommentRepository extends BaseCommentRepository<JournalComment> {
    @Query("select new com.wjk.halo.model.projection.CommentCountProjection(count(comment.id), comment.postId) " +
            "from JournalComment comment " +
            "where comment.postId in ?1 group by comment.postId")
    @NonNull
    @Override
    List<CommentCountProjection> countByPostIds(@NonNull Collection<Integer> postIds);
}
