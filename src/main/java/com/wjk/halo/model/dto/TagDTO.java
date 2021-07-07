package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.Tag;
import lombok.Data;

import java.util.Date;

@Data
public class TagDTO implements OutputConverter<TagDTO, Tag> {

    private Integer id;

    private String name;

    private String slug;

    private String thumbnail;

    private Date createTime;

    private String fullPath;

}
