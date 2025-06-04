package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.helpers.BillingResultHelper;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils;

public class ConsumeAsync implements Runnable {

    private final String token;
    private final ConsumeResponseListener listener;
    private final Repository repository;

    public ConsumeAsync(String token, ConsumeResponseListener listener, Repository repository) {
        this.token = token;
        this.listener = listener;
        this.repository = repository;
    }

    @Override
    public void run() {
        if (token == null || token.isEmpty()) {
            listener.onConsumeResponse(BillingResultHelper.buildBillingResult(ResponseCode.DEVELOPER_ERROR.getValue(),
                BillingResultHelper.ERROR_TYPE_PURCHASE_TOKEN_CANNOT_BE_NULL), null);
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendConsumePurchaseResult(null, ResponseCode.DEVELOPER_ERROR.getValue());
            return;
        }

        try {
            BillingResult billingResult = repository.consumeAsync(token);

            listener.onConsumeResponse(billingResult, token);
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendConsumePurchaseResult(token, billingResult.getResponseCode());
        } catch (ServiceConnectionException e) {
            listener.onConsumeResponse(
                BillingResultHelper.buildBillingResult(ResponseCode.SERVICE_UNAVAILABLE.getValue(),
                    BillingResultHelper.ERROR_TYPE_SERVICE_NOT_AVAILABLE), null);
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendConsumePurchaseResult(token, ResponseCode.SERVICE_UNAVAILABLE.getValue());
        }
    }
}
