package com.appcoins.sdk.billing.listeners;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SDKWebResponseStream {

    private static SDKWebResponseStream instance;

    private final List<Consumer<SDKWebResponse>> collectors = new ArrayList<>();

    private final List<Consumer<SDKWebResponse>> singleCollectors = new ArrayList<>();

    private SDKWebResponse currentValue;

    private SDKWebResponseStream() {
    }

    public static synchronized SDKWebResponseStream getInstance() {
        if (instance == null) {
            instance = new SDKWebResponseStream();
        }
        return instance;
    }

    public void emit(SDKWebResponse value) {
        currentValue = value;
        notifyCollectors(value);
    }

    @Nullable
    public SDKWebResponse lastOrNull() {
        return currentValue;
    }

    public void collect(Consumer<SDKWebResponse> collector) {
        addIfNotPresent(collector);
        if (currentValue != null) {
            collector.accept(currentValue);
        }
    }

    public void collectFromNow(Consumer<SDKWebResponse> collector) {
        addIfNotPresent(collector);
    }

    public void collectFromNowOnce(Consumer<SDKWebResponse> collector) {
        addSingleCollectorIfNotPresent(collector);
    }

    private void notifyCollectors(SDKWebResponse value) {
        for (Consumer<SDKWebResponse> collector : collectors) {
            collector.accept(value);
        }

        for (Consumer<SDKWebResponse> collector : singleCollectors) {
            collector.accept(value);
        }
        singleCollectors.clear();
    }

    public interface Consumer<SDKWebResponse> {
        void accept(SDKWebResponse value);
    }

    private void addIfNotPresent(Consumer<SDKWebResponse> collector) {
        if (!collectors.contains(collector)) {
            collectors.add(collector);
        }
    }

    private void addSingleCollectorIfNotPresent(Consumer<SDKWebResponse> collector) {
        if (!singleCollectors.contains(collector)) {
            singleCollectors.add(collector);
        }
    }
}
