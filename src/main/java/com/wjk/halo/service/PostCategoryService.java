package com.wjk.halo.service;

import com.wjk.halo.model.dto.CategoryWithPostCountDTO;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.entity.PostCategory;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PostCategoryService extends CrudService<PostCategory, Integer> {

    @NonNull
    @Transactional
    List<PostCategory> removeByPostId(@NonNull Integer postId);

    @NonNull
    List<PostCategory> mergeOrCreateByIfAbsent(@NonNull Integer postId, @Nullable Set<Integer> categoryIds);

    @NonNull
    List<CategoryWithPostCountDTO> listCategoryWithPostCountDto(@NonNull Sort sort);

    @NonNull
    @Transactional
    List<PostCategory> removeByCategoryId(@NonNull Integer categoryId);

    @NonNull
    Map<Integer, List<Category>> listCategoryListMap(@Nullable Collection<Integer> postIds);

    @NonNull
    List<Category> listCategoriesBy(@NonNull Integer postId);

    @NonNull
    Page<Post> pagePostBy(@NonNull Integer categoryId, @NonNull PostStatus status, Pageable pageable);


}
