package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;

public interface Billing {

    void queryPurchasesAsync(QueryPurchasesParams queryPurchasesParams,
        PurchasesResponseListener purchasesResponseListener);

    PurchasesResult queryPurchases(String skuType);

    void queryProductDetailsAsync(QueryProductDetailsParams queryProductDetailsParams,
        ProductDetailsResponseListener productDetailsResponseListener);

    void consumeAsync(String purchaseToken, ConsumeResponseListener listener);

    LaunchBillingFlowResult launchBillingFlow(BillingFlowParams params, String payload, String oemid,
        String guestWalletId) throws ServiceConnectionException;

    boolean isReady();

    BillingResult isFeatureSupported(FeatureType feature);

    @Deprecated
    void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
        SkuDetailsResponseListener onSkuDetailsResponseListener);
}

