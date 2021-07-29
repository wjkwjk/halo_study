package com.wjk.halo.service;

import com.wjk.halo.model.dto.CategoryDTO;
import com.wjk.halo.model.entity.Category;
import com.wjk.halo.model.vo.CategoryVO;
import com.wjk.halo.service.base.CrudService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface CategoryService extends CrudService<Category, Integer> {

    @NonNull
    CategoryDTO convertTo(@NonNull Category category);

    @NonNull
    List<CategoryDTO> convertTo(@Nullable List<Category> categories);

    @NonNull
    List<CategoryVO> listAsTree(@NonNull Sort sort);

    @Transactional
    void removeCategoryAndPostCategoryBy(Integer categoryId);

    List<Category> listByParentId(@NonNull Integer id);

}
