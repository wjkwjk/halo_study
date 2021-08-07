package com.wjk.halo.repository.base;

import com.wjk.halo.model.entity.BasePost;
import com.wjk.halo.model.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface BasePostRepository<POST extends BasePost> extends BaseRepository<POST, Integer>{

    long countByStatus(@NonNull PostStatus status);

    boolean existsBySlug(@NonNull String slug);

    boolean existsByIdNotAndSlug(@NonNull Integer id, @NonNull String slug);

    @Query("select sum(p.visits) from BasePost p")
    Long countVisit();

    @Query("select sum(p.likes) from BasePost p")
    Long countLike();

    @NonNull
    Page<POST> findAllByStatus(@NonNull PostStatus status, @NonNull Pageable pageable);

    @Modifying
    @Query("update BasePost p set p.likes = p.likes + :likes where p.id = :postId")
    int updateLikes(@Param("likes") long likes, @Param("postId") @NonNull Integer postId);

    @Modifying
    @Query("update BasePost p set p.status = :status where p.id = :postId")
    int updateStatus(@Param("status") @NonNull PostStatus status, @Param("postId") @NonNull Integer postId);

    @Modifying
    @Query("update BasePost p set p.formatContent = :formatContent where p.id = :postId")
    int updateFormatContent(@Param("formatContent") @NonNull String formatContent, @Param("postId") @NonNull Integer postId);

    @Modifying
    @Query("update BasePost p set p.originalContent = :content where p.id = :postId")
    int updateOriginalContent(@Param("content") @NonNull String content, @Param("postId") @NonNull Integer postId);

    @NonNull
    Optional<POST> getByIdAndStatus(@NonNull Integer id, @NonNull PostStatus status);

    @Modifying
    @Query("update BasePost p set p.visits = p.visits + :visits where p.id = :postId")
    int updateVisit(@Param("visits") long visits, @Param("postId") @NonNull Integer postId);
}
