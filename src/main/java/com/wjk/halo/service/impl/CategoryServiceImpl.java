package com.wjk.halo.service.impl;

import com.google.common.base.Objects;
import com.wjk.halo.exception.AlreadyExistsException;
import com.wjk.halo.exception.NotFoundException;
import com.wjk.halo.model.dto.CategoryDTO;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.vo.CategoryVO;
import com.wjk.halo.repository.CategoryRepository;
import com.wjk.halo.repository.base.BaseRepository;
import com.wjk.halo.service.CategoryService;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.PostCategoryService;
import com.wjk.halo.service.base.AbstractCrudService;
import com.wjk.halo.utils.ServiceUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
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

    //判断分类是否已经存在或者类别的父类是否存在，创建类别，即将类别保存在数据库中
    @Override
    @Transactional
    public Category create(Category category) {
        Assert.notNull(category, "Category to create must not be null");

        long count = categoryRepository.countByName(category.getName());

        if (count>0){
            log.error("Category has exist already: [{}]", category);
            throw new AlreadyExistsException("该分类已存在");
        }

        if (!ServiceUtils.isEmptyId(category.getParentId())){
            count = categoryRepository.countById(category.getParentId());

            if (count == 0){
                log.error("Parent category with id: [{}] was not found, category: [{}]", category.getParentId(), category);
                throw new NotFoundException("Parent category with id = " + category.getParentId() + " was not found");
            }
        }
        return super.create(category);
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

    @Override
    public List<CategoryVO> listAsTree(Sort sort) {
        List<Category> categories = listAll(sort);

        if (CollectionUtils.isEmpty(categories)){
            return Collections.emptyList();
        }
        //生成类别树的根节点
        CategoryVO topLevelCategory = createTopLevelCategory();

        concreteTree(topLevelCategory, categories);

        return topLevelCategory.getChildren();


    }

    @Override
    @Transactional
    public void removeCategoryAndPostCategoryBy(Integer categoryId) {
        List<Category> categories = listByParentId(categoryId);
        if (null != categories && categories.size() > 0){
            categories.forEach(category -> {
                category.setParentId(0);
                update(category);
            });
        }
        removeById(categoryId);
        postCategoryService.removeByCategoryId(categoryId);
    }

    @Override
    public List<Category> listByParentId(Integer id) {
        return categoryRepository.findByParentId(id);
    }

    @NonNull
    private CategoryVO createTopLevelCategory(){
        CategoryVO topCategory = new CategoryVO();
        topCategory.setId(0);
        topCategory.setChildren(new LinkedList<>());
        topCategory.setParentId(-1);
        return topCategory;
    }

    public void concreteTree(CategoryVO parentCategory, List<Category> categories){
        if (CollectionUtils.isEmpty(categories)){
            return;
        }
        //找到当前parentCategory的子节点
        List<Category> children = categories.stream()
                .filter(category -> Objects.equal(parentCategory.getId(), category.getParentId()))
                .collect(Collectors.toList());

        children.forEach(category -> {
            CategoryVO child = new CategoryVO().convertFrom(category);

            if (parentCategory.getChildren() == null){
                parentCategory.setChildren(new LinkedList<>());
            }

            StringBuilder fullPath = new StringBuilder();

            if (optionService.isEnabledAbsolutePath()){
                fullPath.append(optionService.getBlogBaseUrl());
            }

            fullPath.append(URL_SEPARATOR)
                    .append(optionService.getCategoriesPrefix())
                    .append(URL_SEPARATOR)
                    .append(child.getSlug())
                    .append(optionService.getPathSuffix());
            child.setFullPath(fullPath.toString());

            parentCategory.getChildren().add(child);

        });

    }

}
