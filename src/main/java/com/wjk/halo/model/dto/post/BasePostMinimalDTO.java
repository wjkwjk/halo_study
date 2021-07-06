package com.wjk.halo.model.dto.post;

import com.wjk.halo.model.dto.base.OutputConverter;
import com.wjk.halo.model.entity.BasePost;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
public class BasePostMinimalDTO implements OutputConverter<BasePostMinimalDTO, BasePost> {

    private Integer id;

    private String title;

}
