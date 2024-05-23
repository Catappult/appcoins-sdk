package com.appcoins.sdk.billing.listeners;

import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.payflow.PayflowMethodResponse;

import org.jetbrains.annotations.Nullable;

public class PayflowPriorityStream {

    private static PayflowPriorityStream instance;

    @Nullable
    private PayflowMethodResponse value = null;

    @Nullable
    private Consumer<PayflowMethodResponse> collector = null;

    private PayflowPriorityStream() {
    }

    public static synchronized PayflowPriorityStream getInstance() {
        if (instance == null) {
            instance = new PayflowPriorityStream();
        }
        return instance;
    }

    public PayflowMethodResponse value() {
        return value;
    }

    public void emit(@Nullable PayflowMethodResponse value) {
        if (value != null) {
            WalletUtils.setPayflowMethodsList(value.getPaymentFlowList());
        } else {
            WalletUtils.setPayflowMethodsList(null);
        }
        notifyCollectors(value);
    }

    public void collect(Consumer<PayflowMethodResponse> collector) {
        this.collector = collector;
    }

    public void stopCollecting(Consumer<PayflowMethodResponse> collector) {
        this.collector = null;
    }

    private void notifyCollectors(@Nullable PayflowMethodResponse value) {
        this.value = value;
        if (collector != null) {
            collector.accept(value);
        }
    }

    public interface Consumer<PayflowMethodResponse> {
        void accept(@Nullable PayflowMethodResponse value);
    }
}
