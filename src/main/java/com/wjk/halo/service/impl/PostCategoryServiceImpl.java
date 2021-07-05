package com.wjk.halo.service.impl;

import com.wjk.halo.model.entity.PostCategory;
import com.wjk.halo.repository.PostCategoryRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.PostCategoryService;
import com.wjk.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostCategoryServiceImpl extends AbstractCrudService<PostCategory, Integer> implements PostCategoryService {
    private final PostCategoryRepository postCategoryRepository;

    public PostCategoryServiceImpl(PostCategoryRepository postCategoryRepository) {
        super(postCategoryRepository);
        this.postCategoryRepository = postCategoryRepository;
    }

    @Override
    public List<PostCategory> removeByPostId(Integer postId) {
        return postCategoryRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostCategory> mergeOrCreateByIfAbsent(Integer postId, Set<Integer> categoryIds) {

        List<PostCategory> postCategoriesStaging = categoryIds.stream().map(categoryId -> {
            PostCategory postCategory = new PostCategory();
            postCategory.setPostId(postId);
            postCategory.setCategoryId(categoryId);
            return postCategory;
        }).collect(Collectors.toList());

        List<PostCategory> postCategoriesToCreate = new LinkedList<>();
        List<PostCategory> postCategoriesToRemove = new LinkedList<>();

        List<PostCategory> postCategories = postCategoryRepository.findAllByPostId(postId);

        postCategories.forEach(postCategory -> {
            if (!postCategoriesStaging.contains(postCategory)){
                postCategoriesToRemove.add(postCategory);
            }
        });

        postCategoriesStaging.forEach(postCategoryStaging -> {
            if (postCategories.contains(postCategoryStaging)){
                postCategoriesToCreate.add(postCategoryStaging);
            }
        });

        removeAll(postCategoriesToRemove);

        postCategories.removeAll(postCategoriesToRemove);

        postCategories.addAll(createInBatch(postCategoriesToCreate));

        return postCategories;

    }
}
