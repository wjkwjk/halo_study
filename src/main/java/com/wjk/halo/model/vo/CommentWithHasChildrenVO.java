package com.wjk.halo.model.vo;

import com.wjk.halo.model.dto.BaseCommentDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommentWithHasChildrenVO extends BaseCommentDTO {
    private boolean hasChildren;
}
