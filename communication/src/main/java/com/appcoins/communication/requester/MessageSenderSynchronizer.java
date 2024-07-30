package com.appcoins.communication.requester;

import android.os.Parcelable;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MessageSenderSynchronizer {
    private final int timeout;
    private final TaskQueueSynchronizer taskQueueSynchronizer = new TaskQueueSynchronizer();

    public MessageSenderSynchronizer(int timeout) {
        this.timeout = timeout;
    }

    public Parcelable addTaskToQueue(Callable<Parcelable> task) {
        try {
            return taskQueueSynchronizer.executeTask(task, timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
