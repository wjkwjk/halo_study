package com.wjk.halo.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

public class StaticStorageChangedEvent extends ApplicationEvent {

    @Getter
    private final Path staticPath;

    public StaticStorageChangedEvent(Object source, Path staticPath) {
        super(source);
        this.staticPath = staticPath;
    }
}
