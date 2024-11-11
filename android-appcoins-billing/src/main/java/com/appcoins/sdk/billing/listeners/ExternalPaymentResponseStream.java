package com.appcoins.sdk.billing.listeners;

import java.util.ArrayList;
import java.util.List;

import static com.appcoins.sdk.core.logger.Logger.logInfo;

public class ExternalPaymentResponseStream {

    private static ExternalPaymentResponseStream instance;

    private final List<Consumer> collectors = new ArrayList<>();

    private ExternalPaymentResponseStream() {
    }

    public static synchronized ExternalPaymentResponseStream getInstance() {
        if (instance == null) {
            instance = new ExternalPaymentResponseStream();
        }
        return instance;
    }

    public void emit() {
        logInfo("Emitting new value on ExternalPaymentResponseStream.");
        notifyCollectors();
    }

    public void collect(Consumer collector) {
        if (!collectors.contains(collector)) {
            collectors.add(collector);
        }
    }

    public void removeCollector(Consumer collector) {
        collectors.remove(collector);
    }

    public Boolean hasCollectors() {
        return !collectors.isEmpty();
    }

    private void notifyCollectors() {
        for (Consumer collector : collectors) {
            collector.accept();
        }
    }

    public interface Consumer {
        void accept();
    }
}
