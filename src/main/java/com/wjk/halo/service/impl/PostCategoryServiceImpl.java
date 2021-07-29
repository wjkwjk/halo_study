package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.CategoryWithPostCountDTO;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.entity.PostCategory;
import com.wjk.halo.model.projection.CategoryPostCountProjection;
import com.wjk.halo.repository.CategoryRepository;
import com.wjk.halo.repository.PostCategoryRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostCategoryService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.ServiceUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Service
public class PostCategoryServiceImpl extends AbstractCrudService<PostCategory, Integer> implements PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;

    private final CategoryRepository categoryRepository;

    private final OptionService optionService;

    public PostCategoryServiceImpl(PostCategoryRepository postCategoryRepository,
                                   CategoryRepository categoryRepository,
                                   OptionService optionService) {
        super(postCategoryRepository);
        this.postCategoryRepository = postCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.optionService = optionService;
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

    @Override
    public List<CategoryWithPostCountDTO> listCategoryWithPostCountDto(Sort sort) {
        List<Category> categories = categoryRepository.findAll(sort);

        Map<Integer, Long> categoryPostCountMap = ServiceUtils.convertToMap(postCategoryRepository.findPostCount(), CategoryPostCountProjection::getCategoryId, CategoryPostCountProjection::getPostCount);

        return categories.stream()
                .map(category -> {
                    CategoryWithPostCountDTO categoryWithPostCountDTO = new CategoryWithPostCountDTO().convertFrom(category);

                    categoryWithPostCountDTO.setPostCount(categoryPostCountMap.getOrDefault(category.getId(), 0L));

                    StringBuilder fullPath = new StringBuilder();

                    if (optionService.isEnabledAbsolutePath()){
                        fullPath.append(optionService.getBlogBaseUrl());
                    }

                    fullPath.append(URL_SEPARATOR)
                            .append(optionService.getCategoriesPrefix())
                            .append(URL_SEPARATOR)
                            .append(category.getSlug())
                            .append(optionService.getPathSuffix());

                    categoryWithPostCountDTO.setFullPath(fullPath.toString());

                    return categoryWithPostCountDTO;

                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PostCategory> removeByCategoryId(Integer categoryId) {
        return postCategoryRepository.deleteByCategoryId(categoryId);
    }
}
