package com.wjk.halo.service.impl;

import com.wjk.halo.model.dto.CategoryDTO;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.repository.CategoryRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.CategoryService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostCategoryService;
import com.wjk.halo.service.base.AbstractCrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.wjk.halo.model.support.HaloConst.URL_SEPARATOR;

@Slf4j
@Service
public class CategoryServiceImpl extends AbstractCrudService<Category, Integer> implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostCategoryService postCategoryService;
    private final OptionService optionService;

    public CategoryServiceImpl(CategoryRepository categoryRepository, PostCategoryService postCategoryService, OptionService optionService) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
        this.postCategoryService = postCategoryService;
        this.optionService = optionService;
    }

    @Override
    public CategoryDTO convertTo(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO().convertFrom(category);

        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()){
            fullPath.append(optionService.getBlogBaseUrl());
        }

        fullPath.append(URL_SEPARATOR)
                .append(optionService.getCategoriesPrefix())
                .append(URL_SEPARATOR)
                .append(category.getSlug())
                .append(optionService.getPathSuffix());

        categoryDTO.setFullPath(fullPath.toString());

        return categoryDTO;

    }

    public List<CategoryDTO> convertTo(List<Category> categories){
        if (CollectionUtils.isEmpty(categories)){
            return Collections.emptyList();
        }

        return categories.stream()
                .map(this::convertTo)
                .collect(Collectors.toList());
    }

}
