package com.appcoins.sdk.billing.listeners;

import java.util.ArrayList;
import java.util.List;

import static com.appcoins.sdk.core.logger.Logger.logInfo;

public class PaymentResponseStream {

    private static PaymentResponseStream instance;

    private final List<Consumer<SDKPaymentResponse>> collectors = new ArrayList<>();

    private PaymentResponseStream() {
    }

    public static synchronized PaymentResponseStream getInstance() {
        if (instance == null) {
            instance = new PaymentResponseStream();
        }
        return instance;
    }

    public void emit(SDKPaymentResponse value) {
        logInfo("Emitting new value on PaymentResponseStream.");
        notifyCollectors(value);
    }

    public void collect(Consumer<SDKPaymentResponse> collector) {
        if (!collectors.contains(collector)) {
            collectors.add(collector);
        }
    }

    private void notifyCollectors(SDKPaymentResponse value) {
        for (Consumer<SDKPaymentResponse> collector : collectors) {
            collector.accept(value);
        }
    }

    public interface Consumer<SDKPaymentResponse> {
        void accept(SDKPaymentResponse value);
    }
}
