package com.wjk.halo.model.dto;

import com.wjk.halo.model.enums.OptionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class OptionSimpleDTO extends OptionDTO{
    private Integer id;

    private OptionType type;

    private Date createTime;

    private Date updateTime;
}
