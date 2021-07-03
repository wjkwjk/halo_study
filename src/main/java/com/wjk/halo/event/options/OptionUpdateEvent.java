package com.wjk.halo.event.options;

import org.springframework.context.ApplicationEvent;

public class OptionUpdateEvent extends ApplicationEvent {
    public OptionUpdateEvent(Object source) {
        super(source);
    }
}
