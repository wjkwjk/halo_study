package com.wjk.halo.event.theme;

import org.springframework.context.ApplicationEvent;

public class ThemeUpdatedEvent extends ApplicationEvent {
    public ThemeUpdatedEvent(Object source) {
        super(source);
    }
}
