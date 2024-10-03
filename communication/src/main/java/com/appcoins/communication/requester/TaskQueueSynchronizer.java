package com.appcoins.communication.requester;

import static com.appcoins.sdk.core.logger.Logger.logWarning;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskQueueSynchronizer {
    private final BlockingQueue<FutureTask<Parcelable>> taskQueue = new LinkedBlockingQueue<>();

    public TaskQueueSynchronizer() {
        Thread workerThread = new Thread(() -> {
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
            logWarning("Task execution timed out.");
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

