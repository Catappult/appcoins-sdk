package com.appcoins.sdk.billing.listeners;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PurchaseResponseStream {

    private static PurchaseResponseStream instance;

    private final List<Consumer<PurchaseResponse>> collectors = new ArrayList<>();

    private final List<Consumer<PurchaseResponse>> singleCollectors = new ArrayList<>();

    private PurchaseResponse currentValue;

    private PurchaseResponseStream() {
    }

    public static synchronized PurchaseResponseStream getInstance() {
        if (instance == null) {
            instance = new PurchaseResponseStream();
        }
        return instance;
    }

    public void emit(PurchaseResponse value) {
        currentValue = value;
        notifyCollectors(value);
    }

    @Nullable
    public PurchaseResponse lastOrNull() {
        return currentValue;
    }

    public void collect(Consumer<PurchaseResponse> collector) {
        collectors.add(collector);
        if (currentValue != null) {
            collector.accept(currentValue);
        }
    }

    public void collectFromNow(Consumer<PurchaseResponse> collector) {
        collectors.add(collector);
    }

    public void collectFromNowOnce(Consumer<PurchaseResponse> collector) {
        singleCollectors.add(collector);
    }

    private void notifyCollectors(PurchaseResponse value) {
        for (Consumer<PurchaseResponse> collector : collectors) {
            collector.accept(value);
        }

        for (Consumer<PurchaseResponse> collector : singleCollectors) {
            collector.accept(value);
        }
        singleCollectors.clear();
    }

    public interface Consumer<PurchaseResponse> {
        void accept(PurchaseResponse value);
    }
}
