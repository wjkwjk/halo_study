package com.wjk.halo.repository;

import com.wjk.halo.model.entity.Category;
import com.wjk.halo.repository.base.BaseRepository;
import jdk.internal.dynalink.linker.LinkerServices;
import org.springframework.lang.NonNull;

import java.util.List;


public interface CategoryRepository extends BaseRepository<Category, Integer> {

    long countByName(@NonNull String name);

    long countById(@NonNull Integer id);

    List<Category> findByParentId(@NonNull Integer id);

}
