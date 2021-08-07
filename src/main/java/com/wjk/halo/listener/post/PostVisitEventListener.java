package com.wjk.halo.listener.post;

import com.wjk.halo.event.post.PostVisitEvent;
import com.wjk.halo.service.PostService;
import com.wjk.halo.service.base.BasePostService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PostVisitEventListener extends AbstractVisitEventListener{

    public PostVisitEventListener(PostService postService) {
        super(postService);
    }

    @Async
    @EventListener
    public void onPostVisitEvent(PostVisitEvent event) throws InterruptedException{
        handleVisitEvent(event);
    }

}
