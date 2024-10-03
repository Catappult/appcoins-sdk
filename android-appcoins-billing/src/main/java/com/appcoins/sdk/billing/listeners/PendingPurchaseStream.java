package com.appcoins.sdk.billing.listeners;

import android.app.Activity;
import com.appcoins.sdk.billing.BuyItemProperties;
import kotlin.Pair;
import org.jetbrains.annotations.Nullable;

import static com.appcoins.sdk.core.logger.Logger.logInfo;

public class PendingPurchaseStream {

    private static PendingPurchaseStream instance;

    private Pair<Activity, BuyItemProperties> value = null;

    @Nullable
    private Consumer<Pair<Activity, BuyItemProperties>> collector = null;

    private PendingPurchaseStream() {
    }

    public static synchronized PendingPurchaseStream getInstance() {
        if (instance == null) {
            instance = new PendingPurchaseStream();
        }
        return instance;
    }

    public Pair<Activity, BuyItemProperties> value() {
        return value;
    }

    public void emit(
        @Nullable
        Pair<Activity, BuyItemProperties> value) {
        logInfo("Emitting new value on PendingPurchaseStream.");
        notifyCollector(value);
    }

    public void collect(Consumer<Pair<Activity, BuyItemProperties>> collector) {
        this.collector = collector;
    }

    public void stopCollecting() {
        this.collector = null;
    }

    private void notifyCollector(
        @Nullable
        Pair<Activity, BuyItemProperties> value) {
        this.value = value;
        if (collector != null) {
            collector.accept(value);
        }
    }

    public interface Consumer<BuyItemProperties> {
        void accept(
            @Nullable
            BuyItemProperties value);
    }
}
