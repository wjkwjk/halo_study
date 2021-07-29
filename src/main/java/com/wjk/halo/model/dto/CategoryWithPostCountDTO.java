package com.wjk.halo.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CategoryWithPostCountDTO extends CategoryDTO{
    private Long postCount;
}
