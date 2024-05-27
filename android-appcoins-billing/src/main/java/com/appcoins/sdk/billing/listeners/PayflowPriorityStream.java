package com.appcoins.sdk.billing.listeners;

import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PayflowPriorityStream {

    private static PayflowPriorityStream instance;

    @Nullable
    private List<PaymentFlowMethod> value = null;

    @Nullable
    private Consumer<List<PaymentFlowMethod>> collector = null;

    private PayflowPriorityStream() {
    }

    public static synchronized PayflowPriorityStream getInstance() {
        if (instance == null) {
            instance = new PayflowPriorityStream();
        }
        return instance;
    }

    public List<PaymentFlowMethod> value() {
        return value;
    }

    public void emit(@Nullable List<PaymentFlowMethod> value) {
        WalletUtils.setPayflowMethodsList(value);
        notifyCollectors(value);
    }

    public void collect(Consumer<List<PaymentFlowMethod>> collector) {
        this.collector = collector;
    }

    public void stopCollecting() {
        this.collector = null;
    }

    private void notifyCollectors(@Nullable List<PaymentFlowMethod> value) {
        this.value = value;
        if (collector != null) {
            collector.accept(value);
        }
    }

    public interface Consumer<Any> {
        void accept(@Nullable Any value);
    }
}
