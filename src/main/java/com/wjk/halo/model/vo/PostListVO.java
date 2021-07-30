package com.wjk.halo.model.vo;

import com.wjk.halo.model.dto.CategoryDTO;
import com.wjk.halo.model.dto.TagDTO;
import com.wjk.halo.model.dto.post.BasePostSimpleDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostListVO extends BasePostSimpleDTO {
    private Long commentCount;

    private List<TagDTO> tags;

    private List<CategoryDTO> categories;

    private Map<String, Object> metas;
}
