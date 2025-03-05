package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import java.util.List;

public interface Repository {

    PurchasesResult getPurchases(String skuType) throws ServiceConnectionException;

    SkuDetailsResult querySkuDetailsAsync(String skuType, List<String> sku) throws ServiceConnectionException;

    int consumeAsync(String purchaseToken) throws ServiceConnectionException;

    LaunchBillingFlowResult launchBillingFlow(String skuType, String sku, String payload, String oemid,
        String guestWalletId) throws ServiceConnectionException;

    boolean isReady();

    int isFeatureSupported(FeatureType feature) throws ServiceConnectionException;
}
