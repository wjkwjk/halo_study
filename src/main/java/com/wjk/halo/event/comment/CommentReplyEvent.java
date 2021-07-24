package com.wjk.halo.event.comment;

import org.springframework.lang.NonNull;

public class CommentReplyEvent extends AbstractCommentBaseEvent{
    public CommentReplyEvent(Object source, @NonNull Long commentId) {
        super(source, commentId);
    }
}
