package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.Link;
import lombok.Data;

@Data
public class LinkDTO implements OutputConverter<LinkDTO, Link> {
    private Integer id;

    private String name;

    private String url;

    private String logo;

    private String description;

    private String team;

    private Integer priority;
}
