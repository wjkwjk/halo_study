package com.wjk.halo.event.comment;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;


public class AbstractCommentBaseEvent extends ApplicationEvent {

    private final Long commentId;

    public AbstractCommentBaseEvent(Object source, @NonNull Long commentId) {
        super(source);
        this.commentId = commentId;
    }

    @NonNull
    public Long getCommentId(){
        return commentId;
    }

}
