package com.wjk.halo.repository.base;

import com.wjk.halo.model.entity.BasePost;
import com.wjk.halo.model.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

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
}
