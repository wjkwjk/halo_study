package com.wjk.halo.repository;

import com.wjk.halo.model.entity.PostComment;
import com.wjk.halo.model.projection.CommentCountProjection;
import com.wjk.halo.repository.base.BaseCommentRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface PostCommentRepository extends BaseCommentRepository<PostComment> {

    @Query("select new com.wjk.halo.model.projection.CommentCountProjection(count(comment.id), comment.postId) " +
            "from PostComment comment " +
            "where comment.postId in ?1 group by comment.postId")
    @NonNull
    @Override
    List<CommentCountProjection> countByPostIds(@NonNull Collection<Integer> postIds);

    /**
     * 根据时间范围和IP地址统计评论次数
     *
     * @param ipAddress IP地址
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 评论次数
     */
    @Query("SELECT COUNT(id) FROM PostComment WHERE ipAddress=?1 AND updateTime BETWEEN ?2 AND ?3 AND status <> 2")
    int countByIpAndTime(String ipAddress, Date startTime, Date endTime);

}
