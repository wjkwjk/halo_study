package com.wjk.halo.model.vo;

import com.wjk.halo.model.dto.BaseMetaDTO;
import com.wjk.halo.model.dto.post.BasePostDetailDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class SheetDetailVO extends BasePostDetailDTO {
    private Set<Long> metaIds;

    private List<BaseMetaDTO> metas;
}
