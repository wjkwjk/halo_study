package com.wjk.halo.model.vo;

import com.wjk.halo.model.dto.BaseCommentDTO;
import com.wjk.halo.model.dto.post.BasePostMinimalDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class PostCommentWithPostVO extends BaseCommentDTO {
    private BasePostMinimalDTO post;
}
