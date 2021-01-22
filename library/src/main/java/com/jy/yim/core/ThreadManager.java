package com.jy.yim.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description 线程管理
 * @date: 2020/4/30 17:30
 * @author: jy
 */
public class ThreadManager {

    private static ThreadManager instance;
    private ExecutorService mThreadPool;

    private ThreadManager() {
        mThreadPool = Executors.newCachedThreadPool();
    }

    public synchronized static ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    public void execute(Runnable task) {
        if (task == null) {
            return;
        }
        mThreadPool.execute(task);
    }

    public void release() {
        try {
            mThreadPool.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
