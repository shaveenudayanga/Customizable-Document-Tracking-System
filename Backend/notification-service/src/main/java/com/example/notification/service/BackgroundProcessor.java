package com.example.notification.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BackgroundProcessor {

    private final ExecutorService executor;

    public BackgroundProcessor() {
        // Use a cached thread pool
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Submit a task to run asynchronously. The caller provides the runnable so we avoid
     * direct service dependencies here and break circular references.
     */
    public void submit(Runnable task) {
        executor.submit(task);
    }
}
