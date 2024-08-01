package com.appcoins.communication.requester;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskQueueSynchronizer {
    private final String TAG = TaskQueueSynchronizer.class.getSimpleName();
    private final BlockingQueue<FutureTask<Parcelable>> taskQueue = new LinkedBlockingQueue<>();
    private final Thread workerThread;

    public TaskQueueSynchronizer() {
        workerThread = new Thread(() -> {
            try {
                while (true) {
                    FutureTask<Parcelable> task = taskQueue.take();
                    task.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        workerThread.start();
    }

    public Parcelable executeTask(Callable<Parcelable> task, long timeout, TimeUnit timeUnit) throws Exception {
        FutureTask<Parcelable> futureTask = new FutureTask<>(task);
        taskQueue.put(futureTask);

        try {
            return futureTask.get(timeout, timeUnit);
        } catch (TimeoutException e) {
            Log.w(TAG, "Task execution timed out.");
            futureTask.cancel(true);
            return new Parcelable() {
                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel parcel, int i) {
                }
            };
        }
    }
}

