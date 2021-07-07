package com.wjk.halo.model.dto.post;

import com.wjk.halo.model.entity.Log;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BasePostDetailDTO extends BasePostSimpleDTO{
    private Long commentCount;

}
