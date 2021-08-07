package com.wjk.halo.event.post;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;

public class AbstractVisitEvent extends ApplicationEvent {

    private final Integer id;

    public AbstractVisitEvent(@NonNull Object source, @NonNull Integer id) {
        super(source);
        this.id = id;
    }

    @NonNull
    public Integer getId() {
        return id;
    }
}
