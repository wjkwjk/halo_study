package com.wjk.halo.repository;

import com.wjk.halo.model.entity.PostCategory;
import com.wjk.halo.model.projection.CategoryPostCountProjection;
import com.wjk.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;

public interface PostCategoryRepository extends BaseRepository<PostCategory, Integer> {

    @NonNull
    List<PostCategory> deleteByPostId(@NonNull Integer postId);

    @NonNull
    List<PostCategory> findAllByPostId(@NonNull Integer postId);

    @Query("select new com.wjk.halo.model.projection.CategoryPostCountProjection(count(pc.postId), pc.categoryId) from PostCategory pc group by pc.categoryId")
    @NonNull
    List<CategoryPostCountProjection> findPostCount();

}
