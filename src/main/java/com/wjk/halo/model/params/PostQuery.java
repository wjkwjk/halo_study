package com.wjk.halo.model.params;

import com.wjk.halo.model.enums.PostStatus;
import lombok.Data;

@Data
public class PostQuery {
    /**
     * Keyword.
     */
    private String keyword;

    /**
     * Post status.
     */
    private PostStatus status;

    /**
     * Category id.
     */
    private Integer categoryId;
}
