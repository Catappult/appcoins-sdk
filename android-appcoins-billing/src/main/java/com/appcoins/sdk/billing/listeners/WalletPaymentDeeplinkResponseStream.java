package com.appcoins.sdk.billing.listeners;

import java.util.ArrayList;
import java.util.List;

import static com.appcoins.sdk.core.logger.Logger.logInfo;

public class WalletPaymentDeeplinkResponseStream {

    private static WalletPaymentDeeplinkResponseStream instance;

    private final List<Consumer<Integer>> collectors = new ArrayList<>();

    private WalletPaymentDeeplinkResponseStream() {
    }

    public static synchronized WalletPaymentDeeplinkResponseStream getInstance() {
        if (instance == null) {
            instance = new WalletPaymentDeeplinkResponseStream();
        }
        return instance;
    }

    public void emit(Integer value) {
        logInfo("Emitting new value on WalletPaymentDeeplinkResponseStream.");
        notifyCollectors(value);
    }

    public void collect(Consumer<Integer> collector) {
        if (!collectors.contains(collector)) {
            collectors.add(collector);
        }
    }

    public void removeCollector(Consumer<Integer> collector) {
        collectors.remove(collector);
    }

    private void notifyCollectors(Integer value) {
        for (Consumer<Integer> collector : collectors) {
            collector.accept(value);
        }
    }

    public interface Consumer<Integer> {
        void accept(Integer value);
    }
}
