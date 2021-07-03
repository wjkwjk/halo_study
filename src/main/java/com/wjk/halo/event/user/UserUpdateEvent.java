package com.wjk.halo.event.user;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;

public class UserUpdateEvent extends ApplicationEvent {
    private final Integer userId;

    public UserUpdateEvent(Object source, @NonNull Integer userId) {
        super(source);
        this.userId = userId;
    }

    @NonNull
    public Integer getUserId(){
        return userId;
    }
}
