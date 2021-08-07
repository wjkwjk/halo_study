package com.wjk.halo.event.options;

import org.springframework.context.ApplicationEvent;

public class OptionUpdatedEvent extends ApplicationEvent {

    public OptionUpdatedEvent(Object source) {
        super(source);
    }

}
