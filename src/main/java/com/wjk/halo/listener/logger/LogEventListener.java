package com.wjk.halo.listener.logger;

import com.wjk.halo.event.logger.LogEvent;
import com.wjk.halo.model.entity.Log;
import com.wjk.halo.service.LogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class LogEventListener {
    private final LogService logService;

    public LogEventListener(LogService logService) {
        this.logService = logService;
    }

    @EventListener
    @Async
    public void onApplicationEvent(LogEvent event){
        Log logToCreate = event.getLogParam().convertTo();
        logService.create(logToCreate);
    }


}
