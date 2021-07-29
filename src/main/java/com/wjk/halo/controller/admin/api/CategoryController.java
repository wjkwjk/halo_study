package com.wjk.halo.controller.admin.api;

import com.wjk.halo.service.CategoryService;
import com.wjk.halo.service.PostCategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private final PostCategoryService postCategoryService;

    public CategoryController(CategoryService categoryService, PostCategoryService postCategoryService) {
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
    }
}
