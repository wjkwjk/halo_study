package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Sheet;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.repository.base.BasePostRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface SheetRepository extends BasePostRepository<Sheet> {

    @Override
    @Query("select sum(p.visits) from Sheet p")
    Long countVisit();

    @Override
    @Query("select sum(p.likes) from Sheet p")
    Long countLike();

    @NonNull
    @Override
    Optional<Sheet> getBySlugAndStatus(@NonNull String slug, @NonNull PostStatus status);

}
