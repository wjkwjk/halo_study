package com.wjk.halo.listener.post;

import com.wjk.halo.event.post.SheetVisitEvent;
import com.wjk.halo.service.SheetService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SheetVisitEventListener extends AbstractVisitEventListener {

    protected SheetVisitEventListener(SheetService sheetService) {
        super(sheetService);
    }

    @Async
    @EventListener
    public void onSheetVisitEvent(SheetVisitEvent event) throws InterruptedException {
        handleVisitEvent(event);
    }

}