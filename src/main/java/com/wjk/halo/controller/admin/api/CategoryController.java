package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.CategoryDTO;
import com.wjk.halo.model.vo.CategoryVO;
import com.wjk.halo.service.CategoryService;
import com.wjk.halo.service.PostCategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("tree_view")
    public List<CategoryVO> listAsTree(@SortDefault(sort = "name", direction = ASC) Sort sort){
        return categoryService.listAsTree(sort);
    }

}
