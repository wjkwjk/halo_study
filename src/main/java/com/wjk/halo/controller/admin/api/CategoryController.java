package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.CategoryDTO;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.params.CategoryParam;
import com.wjk.halo.model.vo.CategoryVO;
import com.wjk.halo.service.CategoryService;
import com.wjk.halo.service.PostCategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private final PostCategoryService postCategoryService;

    public CategoryController(CategoryService categoryService, PostCategoryService postCategoryService) {
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
    }

    @GetMapping("{categoryId:\\d+}")
    public CategoryDTO getBy(@PathVariable("categoryId") Integer categoryId){
        return categoryService.convertTo(categoryService.getById(categoryId));
    }

    @GetMapping
    public List<? extends CategoryDTO> listAll(
            @SortDefault(sort = "createTime", direction = DESC) Sort sort,
            @RequestParam(name = "more", required = false, defaultValue = "false") boolean more){
        if (more){
            return postCategoryService.listCategoryWithPostCountDto(sort);
        }
        return categoryService.convertTo(categoryService.listAll(sort));
    }
    //展示所有最大的类别
    @GetMapping("tree_view")
    public List<CategoryVO> listAsTree(@SortDefault(sort = "name", direction = ASC) Sort sort){
        return categoryService.listAsTree(sort);
    }

    @PostMapping
    public CategoryDTO createBy(@RequestBody @Valid CategoryParam categoryParam){
        Category category = categoryParam.convertTo();
        return categoryService.convertTo(categoryService.create(category));
    }

    @PutMapping("{categoryId:\\d+}")
    public CategoryDTO updateBy(@PathVariable("categoryId") Integer categoryId,
                                @RequestBody @Valid CategoryParam categoryParam){
        Category categoryToUpdate = categoryService.getById(categoryId);
        categoryParam.update(categoryToUpdate);
        return categoryService.convertTo(categoryService.update(categoryToUpdate));
    }

    @DeleteMapping("{categoryId:\\d+}")
    public void deletePermanently(@PathVariable("categoryId") Integer categoryId){
        categoryService.removeCategoryAndPostCategoryBy(categoryId);
    }

}
