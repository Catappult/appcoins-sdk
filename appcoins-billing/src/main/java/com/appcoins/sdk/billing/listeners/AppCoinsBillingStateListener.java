package com.appcoins.sdk.billing.listeners;

import com.appcoins.sdk.billing.BillingResult;

public interface AppCoinsBillingStateListener {
    void onBillingSetupFinished(BillingResult billingResult);

    void onBillingServiceDisconnected();
}
