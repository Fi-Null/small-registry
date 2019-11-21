package com.small.registry.admin.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 3:16 PM
 */
public class ThreadPoolUtil {

    /**
     *  create thrad pool
     * @param nThreads
     * @param threadName
     * @return
     */
    public static ExecutorService createThreadPool(int nThreads, String threadName) {

        ExecutorService executorService = new ThreadPoolExecutor(nThreads, 300, 5, TimeUnit.MINUTES, new
                LinkedBlockingQueue<>(), new ThreadFactory() {

            private AtomicInteger count = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, threadName + "_" + count.incrementAndGet());
            }
        });
        return executorService;
    }
}
