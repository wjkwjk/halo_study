package com.wjk.halo.model.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommentPage<T> extends PageImpl<T> {
    private final long commentCount;

    public CommentPage(List<T> content, Pageable pageable, long topTotal, long commentCount) {
        super(content, pageable, topTotal);

        this.commentCount = commentCount;
    }

    public CommentPage(List<T> content, long commentCount) {
        super(content);

        this.commentCount = commentCount;
    }
}
