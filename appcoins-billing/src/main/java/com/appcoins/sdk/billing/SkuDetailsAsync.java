package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.helpers.AnalyticsMappingHelper;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils;
import java.util.ArrayList;

import static com.appcoins.sdk.core.logger.Logger.logError;

public class SkuDetailsAsync implements Runnable {

    private final Repository repository;
    private final SkuDetailsResponseListener skuDetailsResponseListener;
    private final SkuDetailsParams skuDetailsParams;

    public SkuDetailsAsync(SkuDetailsParams skuDetailsParams, SkuDetailsResponseListener skuDetailsResponseListener,
        Repository repository) {
        this.skuDetailsParams = skuDetailsParams;
        this.skuDetailsResponseListener = skuDetailsResponseListener;
        this.repository = repository;
    }

    @Override
    public void run() {
        try {
            SkuDetailsResult response = getSkuDetails();

            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendQuerySkuDetailsResult(new AnalyticsMappingHelper().mapSkuDetailsToListOfStrings(response));

            skuDetailsResponseListener.onSkuDetailsResponse(response.getResponseCode(), response.getSkuDetailsList());
        } catch (ServiceConnectionException e) {
            logError("Service is not ready to request SkuDetails: " + e);
            skuDetailsResponseListener.onSkuDetailsResponse(ResponseCode.SERVICE_UNAVAILABLE.getValue(),
                new ArrayList<>());
        }
    }

    private SkuDetailsResult getSkuDetails() throws ServiceConnectionException {
        return repository.querySkuDetailsAsync(skuDetailsParams.getItemType(), skuDetailsParams.getMoreItemSkus());
    }
}
