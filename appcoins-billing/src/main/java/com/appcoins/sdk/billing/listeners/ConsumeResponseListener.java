package com.appcoins.sdk.billing.listeners;

import com.appcoins.sdk.billing.BillingResult;

public interface ConsumeResponseListener {

    void onConsumeResponse(BillingResult billingResult, String purchaseToken);
}
