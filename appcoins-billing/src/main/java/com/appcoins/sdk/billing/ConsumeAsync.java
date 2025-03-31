package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
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
            listener.onConsumeResponse(ResponseCode.DEVELOPER_ERROR.getValue(), null);
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendConsumePurchaseResult(null, ResponseCode.DEVELOPER_ERROR.getValue());
            return;
        }

        try {
            int response = repository.consumeAsync(token);

            listener.onConsumeResponse(response, token);
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendConsumePurchaseResult(token, response);
        } catch (ServiceConnectionException e) {
            listener.onConsumeResponse(ResponseCode.SERVICE_UNAVAILABLE.getValue(), null);
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendConsumePurchaseResult(token, ResponseCode.SERVICE_UNAVAILABLE.getValue());
        }
    }
}
