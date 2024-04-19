package com.appcoins.sdk.billing.listeners;

import java.util.ArrayList;
import java.util.List;

public class SDKWebResponseStream {

    private static SDKWebResponseStream instance;

    private final List<Consumer<SDKWebResponse>> collectors = new ArrayList<>();

    private SDKWebResponseStream() {
    }

    public static synchronized SDKWebResponseStream getInstance() {
        if (instance == null) {
            instance = new SDKWebResponseStream();
        }
        return instance;
    }

    public void emit(SDKWebResponse value) {
        notifyCollectors(value);
    }

    public void collect(Consumer<SDKWebResponse> collector) {
        if (!collectors.contains(collector)) {
            collectors.add(collector);
        }
    }

    private void notifyCollectors(SDKWebResponse value) {
        for (Consumer<SDKWebResponse> collector : collectors) {
            collector.accept(value);
        }
    }

    public interface Consumer<SDKWebResponse> {
        void accept(SDKWebResponse value);
    }
}
