package com.wjk.halo.model.params;

import com.wjk.halo.model.enums.CommentStatus;
import lombok.Data;

@Data
public class CommentQuery {
    private String keyword;

    private CommentStatus status;
}
