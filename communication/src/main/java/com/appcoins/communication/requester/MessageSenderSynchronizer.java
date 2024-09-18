package com.appcoins.communication.requester;

import static com.appcoins.sdk.core.logger.Logger.logError;

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
            logError("Failed to send synchronized message: " + e);
            return null;
        }
    }
}
