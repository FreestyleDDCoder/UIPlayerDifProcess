package com.example.mediaserverprocess.utils;

import android.annotation.SuppressLint;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : uidq1846
 * @e-mail : liangzhanncu@163.com
 * @date :  2019-2-19  15:54
 * @desc :  供给多媒体使用的线程池
 * @version: 1.0
 */
public class MediaThreadPoolExecutorUtils {
    private static final String TAG = "MediaThreadPoolExecutorUtils";
    private static ExecutorService mExecutorService;
    private static MediaThreadPoolExecutorUtils mMediaThreadPoolExecutorUtils;
    private static int NUMBER_OF_CORE = /*Runtime.getRuntime().availableProcessors()*/2;
    private final static int KEEP_ALIVE_TIME = 30;
    private static TimeUnit KEEP_ALIVE_TIME_UTILS = TimeUnit.SECONDS;
    private static BlockingQueue TASK_QUEUE = new LinkedBlockingQueue<Runnable>();

    public static MediaThreadPoolExecutorUtils getInstance() {
        if (mMediaThreadPoolExecutorUtils == null) {
            mMediaThreadPoolExecutorUtils = new MediaThreadPoolExecutorUtils();
        }
        return mMediaThreadPoolExecutorUtils;
    }

    @SuppressLint("LongLogTag")
    private MediaThreadPoolExecutorUtils() {
        if (mExecutorService == null) {
            Log.i(TAG, "MediaThreadPoolExecutorUtils NUMBER_OF_CORE ==" + NUMBER_OF_CORE);
            mExecutorService = new ThreadPoolExecutor(NUMBER_OF_CORE, NUMBER_OF_CORE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UTILS, TASK_QUEUE,
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(final Runnable r) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                                    r.run();
                                }
                            });
                            if (t.isDaemon()) {
                                t.setDaemon(false);
                            }
                            return t;
                        }
                    });
        }
    }

    public Future<?> submit(Runnable task) {
        showTaskStatusLog();
        return mExecutorService.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        showTaskStatusLog();
        return mExecutorService.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        showTaskStatusLog();
        return mExecutorService.submit(task, result);
    }

    private void showTaskStatusLog() {
        Log.d(TAG, "submit() getActiveCount = " + ((ThreadPoolExecutor) mExecutorService).getActiveCount() + "##taskCount ==" + ((ThreadPoolExecutor) mExecutorService).getTaskCount() + "##completedTaskCount ==" + ((ThreadPoolExecutor) mExecutorService).getCompletedTaskCount());
    }

    public ExecutorService getThreadPoolExecutor() {
        return mExecutorService;
    }
}
