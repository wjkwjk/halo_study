package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.CategoryWithPostCountDTO;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.entity.Post;
import com.wjk.halo.model.entity.PostCategory;
import com.wjk.halo.model.enums.PostStatus;
import com.wjk.halo.model.projection.CategoryPostCountProjection;
import com.wjk.halo.repository.CategoryRepository;
import com.wjk.halo.repository.PostCategoryRepository;
import com.wjk.halo.repository.PostRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostCategoryService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.ServiceUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Service
public class PostCategoryServiceImpl extends AbstractCrudService<PostCategory, Integer> implements PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;

    private PostRepository postRepository;

    private final CategoryRepository categoryRepository;

    private final OptionService optionService;

    public PostCategoryServiceImpl(PostCategoryRepository postCategoryRepository,
                                   PostRepository postRepository,
                                   CategoryRepository categoryRepository,
                                   OptionService optionService) {
        super(postCategoryRepository);
        this.postCategoryRepository = postCategoryRepository;
        this.postRepository = postRepository;
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
            if (!postCategories.contains(postCategoryStaging)){
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
        //获得每个类别以及所包括的post数
        Map<Integer, Long> categoryPostCountMap = ServiceUtils.convertToMap(postCategoryRepository.findPostCount(), CategoryPostCountProjection::getCategoryId, CategoryPostCountProjection::getPostCount);

        return categories.stream()
                .map(category -> {
                    CategoryWithPostCountDTO categoryWithPostCountDTO = new CategoryWithPostCountDTO().convertFrom(category);
                    //设置category对应的post数
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

    @Override
    public Map<Integer, List<Category>> listCategoryListMap(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)){
            return Collections.emptyMap();
        }

        // Find all post categories
        List<PostCategory> postCategories = postCategoryRepository.findAllByPostIdIn(postIds);

        // Fetch category ids
        Set<Integer> categoryIds = ServiceUtils.fetchProperty(postCategories, PostCategory::getCategoryId);

        // Find all categories
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        //用Category的Id作为键，Category本身作为值
        // Convert to category map
        Map<Integer, Category> categoryMap = ServiceUtils.convertToMap(categories, Category::getId);

        // Create category list map
        Map<Integer, List<Category>> categoryListMap = new HashMap<>();

        // Foreach and collect
        postCategories.forEach(postCategory -> categoryListMap.computeIfAbsent(postCategory.getPostId(), postId -> new LinkedList<>())
        .add(categoryMap.get(postCategory.getCategoryId())));

        return categoryListMap;

    }

    @Override
    public List<Category> listCategoriesBy(Integer postId) {
        Set<Integer> categoryIds = postCategoryRepository.findAllCategoryIdsByPostId(postId);
        return categoryRepository.findAllById(categoryIds);
    }

    @Override
    public Page<Post> pagePostBy(Integer categoryId, PostStatus status, Pageable pageable) {
        Set<Integer> postIds = postCategoryRepository.findAllPostIdsByCategoryId(categoryId, status);
        return postRepository.findAllByIdIn(postIds, pageable);
    }
}
