package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.BaseMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@EqualsAndHashCode
public class BaseMetaDTO implements OutputConverter<BaseMetaDTO, BaseMeta> {
    private Long id;
    private Integer postId;
    private String key;
    private String value;
    private Date createTime;
}
