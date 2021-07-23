package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Category;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;


public interface CategoryRepository extends BaseRepository<Category, Integer> {

    long countByName(@NonNull String name);

    long countById(@NonNull Integer id);

}
