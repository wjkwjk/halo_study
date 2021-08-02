package com.wjk.halo.model.vo;

import com.wjk.halo.model.dto.post.BasePostSimpleDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SheetListVO extends BasePostSimpleDTO {
    private Long commentCount;
}
