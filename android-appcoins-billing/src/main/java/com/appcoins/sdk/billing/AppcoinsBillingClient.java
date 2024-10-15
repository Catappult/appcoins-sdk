package com.appcoins.sdk.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;

public interface AppcoinsBillingClient {
    PurchasesResult queryPurchases(String skuType);

    VoidedPurchasesResult queryVoidedPurchases(Long startTimeInMillis);

    void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
        SkuDetailsResponseListener onSkuDetailsResponseListener);

    void consumeAsync(String token, ConsumeResponseListener consumeResponseListener);

    /**
     * This method can't run on the main thread
     */
    int launchBillingFlow(Activity activity, BillingFlowParams billingFlowParams);

    void startConnection(AppCoinsBillingStateListener listener);

    void endConnection();

    boolean isReady();

    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    boolean isAppUpdateAvailable();

    void launchAppUpdateStore(Context context);

    void launchAppUpdateDialog(Context context);
}
