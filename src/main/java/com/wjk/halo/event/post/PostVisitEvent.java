package com.wjk.halo.event.post;

import org.springframework.lang.NonNull;

public class PostVisitEvent extends AbstractVisitEvent{

    public PostVisitEvent(Object source, @NonNull Integer id) {
        super(source, id);
    }
}
