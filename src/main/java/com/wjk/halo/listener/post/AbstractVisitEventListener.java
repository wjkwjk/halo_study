package com.wjk.halo.listener.post;

import com.wjk.halo.event.post.AbstractVisitEvent;
import com.wjk.halo.service.base.BasePostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public abstract class AbstractVisitEventListener {

    private final Map<Integer, BlockingQueue<Integer>> visitQueueMap;

    private final Map<Integer, PostVisitTask> visitTaskMap;

    private final BasePostService basePostService;

    private final ExecutorService executor;

    public AbstractVisitEventListener(BasePostService basePostService) {
        this.basePostService = basePostService;

        int initCapacity = 8;

        long count = basePostService.count();

        if (count < initCapacity){
            initCapacity = (int) count;
        }

        visitQueueMap = new ConcurrentHashMap<>(initCapacity << 1);
        visitTaskMap = new ConcurrentHashMap<>(initCapacity << 1);

        this.executor = Executors.newCachedThreadPool();

    }

    protected void handleVisitEvent(@NonNull AbstractVisitEvent event) throws InterruptedException{
        Integer id = event.getId();

        log.debug("Received a visit event, post id: [{}]", id);

        BlockingQueue<Integer> postVisitQueue = visitQueueMap.computeIfAbsent(id, this::createEmptyQueue);

        visitTaskMap.computeIfAbsent(id, this::createPostVisitTask);

        postVisitQueue.put(id);

    }

    private BlockingQueue<Integer> createEmptyQueue(Integer postId){
        return new LinkedBlockingQueue<>();
    }

    private PostVisitTask createPostVisitTask(Integer postId){
        PostVisitTask postVisitTask = new PostVisitTask(postId);

        executor.execute(postVisitTask);

        log.debug("Created a new post visit task for post id: [{}]", postId);
        return postVisitTask;
    }

    private class PostVisitTask implements Runnable{

        private final Integer id;

        public PostVisitTask(Integer id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    BlockingQueue<Integer> postVisitQueue = visitQueueMap.get(id);
                    Integer postId = postVisitQueue.take();

                    log.debug("Took a new visit for post id: [{}]", postId);

                    basePostService.increaseVisit(postId);

                    log.debug("Increased visits for post id: [{}]", postId);
                }catch (InterruptedException e){
                    log.debug("Post visit task: " + Thread.currentThread().getName() + " was interrupted", e);
                }
            }
            log.debug("Thread: [{}] has been interrupted", Thread.currentThread().getName());
        }
    }

}
