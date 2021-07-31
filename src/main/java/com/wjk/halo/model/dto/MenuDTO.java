package com.wjk.halo.model.dto;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.Menu;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public class MenuDTO implements OutputConverter<MenuDTO, Menu> {
    private Integer id;

    private String name;

    private String url;

    private Integer priority;

    private String target;

    private String icon;

    private Integer parentId;

    private String team;
}
