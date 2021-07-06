package com.wjk.halo.model.vo;

import com.wjk.halo.model.dto.BaseMetaDTO;
import com.wjk.halo.model.dto.CategoryDTO;
import com.wjk.halo.model.dto.TagDTO;
import com.wjk.halo.model.dto.post.BasePostDetailDTO;
import com.wjk.halo.utils.ServiceUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PostDetailVO extends BasePostDetailDTO {
    private Set<Integer> tagIds;

    private List<TagDTO> tags;

    private Set<Integer> categoryIds;

    private List<CategoryDTO> categories;

    private Set<Long> metaIds;

    private List<BaseMetaDTO> metas;
}
