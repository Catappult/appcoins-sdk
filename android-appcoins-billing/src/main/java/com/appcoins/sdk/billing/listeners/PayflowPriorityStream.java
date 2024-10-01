package com.appcoins.sdk.billing.listeners;

import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

import static com.appcoins.sdk.core.logger.Logger.logInfo;

public class PayflowPriorityStream {

    private static PayflowPriorityStream instance;

    private boolean isFirstValue = true;

    @Nullable private ArrayList<PaymentFlowMethod> value = null;

    @Nullable private Consumer<ArrayList<PaymentFlowMethod>> collector = null;

    private PayflowPriorityStream() {
    }

    public static synchronized PayflowPriorityStream getInstance() {
        if (instance == null) {
            instance = new PayflowPriorityStream();
        }
        return instance;
    }

    public ArrayList<PaymentFlowMethod> value() {
        return value;
    }

    public void emit(@Nullable ArrayList<PaymentFlowMethod> value) {
        logInfo("Emitting new value on PayflowPriorityStream.");
        if (valueHasChanged(value) || isFirstValue) {
            logInfo("Value of PayflowPriorityStream changed or isFirstValue.");
            WalletUtils.setPayflowMethodsList(value);
            notifyCollectors(value);
        }
    }

    private boolean valueHasChanged(@Nullable ArrayList<PaymentFlowMethod> newValue) {
        if (value != null) {
            return value.equals(newValue);
        } else {
            return newValue != null;
        }
    }

    public void collect(Consumer<ArrayList<PaymentFlowMethod>> collector) {
        prepareStream();
        this.collector = collector;
    }

    public void stopCollecting() {
        this.collector = null;
    }

    private void prepareStream() {
        isFirstValue = true;
    }

    private void notifyCollectors(@Nullable ArrayList<PaymentFlowMethod> value) {
        this.value = value;
        if (collector != null) {
            collector.accept(value);
        }
    }

    public interface Consumer<Any> {
        void accept(@Nullable Any value);
    }
}
