package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Tag;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface TagRepository extends BaseRepository<Tag, Integer> {
    long countByNameOrSlug(@NonNull String name, @NonNull String slug);

    Optional<Tag> getBySlug(@NonNull String slug);
}
