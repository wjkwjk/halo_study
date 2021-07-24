package com.wjk.halo.event.comment;

import org.springframework.lang.NonNull;

public class CommentNewEvent extends AbstractCommentBaseEvent{
    public CommentNewEvent(Object source, @NonNull Long commentId) {
        super(source, commentId);
    }
}
