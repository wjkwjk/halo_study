package com.wjk.halo.repository;

import com.wjk.halo.model.entity.PostCategory;
import com.wjk.halo.model.projection.CategoryPostCountProjection;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PostCategoryRepository extends BaseRepository<PostCategory, Integer> {

    @NonNull
    List<PostCategory> deleteByPostId(@NonNull Integer postId);

    @NonNull
    List<PostCategory> findAllByPostId(@NonNull Integer postId);

    @Query("select new com.wjk.halo.model.projection.CategoryPostCountProjection(count(pc.postId), pc.categoryId) from PostCategory pc group by pc.categoryId")
    @NonNull
    List<CategoryPostCountProjection> findPostCount();

    @NonNull
    List<PostCategory> deleteByCategoryId(@NonNull Integer categoryId);

    @NonNull
    List<PostCategory> findAllByPostIdIn(@NonNull Collection<Integer> postIds);

    @NonNull
    @Query("select postCategory.categoryId from PostCategory postCategory where postCategory.postId = ?1")
    Set<Integer> findAllCategoryIdsByPostId(@NonNull Integer postId);

}
