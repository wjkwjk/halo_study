package com.wjk.halo.event.theme;

import org.springframework.context.ApplicationEvent;

public class ThemeActivatedEvent extends ApplicationEvent {
    public ThemeActivatedEvent(Object source) {
        super(source);
    }

}
