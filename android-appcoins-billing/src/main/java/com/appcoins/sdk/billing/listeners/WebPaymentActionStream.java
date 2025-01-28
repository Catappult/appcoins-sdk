package com.appcoins.sdk.billing.listeners;

import java.util.ArrayList;
import java.util.List;

import static com.appcoins.sdk.core.logger.Logger.logInfo;

public class WebPaymentActionStream {

    private static WebPaymentActionStream instance;

    private final List<Consumer<String>> collectors = new ArrayList<>();

    private WebPaymentActionStream() {
    }

    public static synchronized WebPaymentActionStream getInstance() {
        if (instance == null) {
            instance = new WebPaymentActionStream();
        }
        return instance;
    }

    public void emit(String value) {
        logInfo("Emitting new value on WebPaymentActionStream.");
        notifyCollectors(value);
    }

    public void collect(Consumer<String> collector) {
        if (!collectors.contains(collector)) {
            collectors.add(collector);
        }
    }

    private void notifyCollectors(String value) {
        for (Consumer<String> collector : collectors) {
            collector.acceptWebPaymentActionStream(value);
        }
    }

    public interface Consumer<String> {
        void acceptWebPaymentActionStream(String value);
    }
}
