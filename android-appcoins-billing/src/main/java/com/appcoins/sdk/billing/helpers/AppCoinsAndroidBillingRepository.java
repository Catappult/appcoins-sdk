package com.appcoins.sdk.billing.helpers;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.appcoins.billing.AppcoinsBilling;
import com.appcoins.sdk.billing.ConnectionLifeCycle;
import com.appcoins.sdk.billing.FeatureType;
import com.appcoins.sdk.billing.LaunchBillingFlowResult;
import com.appcoins.sdk.billing.PurchasesResult;
import com.appcoins.sdk.billing.Repository;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.SkuDetailsResult;
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.service.WalletBillingService;
import com.appcoins.sdk.billing.usecases.IsFeatureSupported;
import com.appcoins.sdk.billing.usecases.RetryFailedRequests;
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils;
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureStep;
import java.util.List;

import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logError;
import static com.appcoins.sdk.core.logger.Logger.logInfo;

class AppCoinsAndroidBillingRepository implements Repository, ConnectionLifeCycle {
    private final int apiVersion;
    private final String packageName;
    private AppcoinsBilling service;
    private boolean isServiceReady;

    public AppCoinsAndroidBillingRepository(int apiVersion, String packageName) {
        logInfo(String.format("Initializing apiVersion:%s packageName:%s", apiVersion, packageName));
        this.apiVersion = apiVersion;
        this.packageName = packageName;
    }

    @Override
    public void onConnect(ComponentName name, IBinder service, final AppCoinsBillingStateListener listener) {
        logInfo(String.format("Billing Connected className:%s service:%s", name.getClassName(), service.getClass()
            .getCanonicalName()));
        this.service = new WalletBillingService(service, name.getClassName());
        isServiceReady = true;
        RetryFailedRequests.INSTANCE.invoke();
        logInfo("Billing Connected, notifying client onBillingSetupFinished(ResponseCode.OK)");
        listener.onBillingSetupFinished(ResponseCode.OK.getValue());
    }

    @Override
    public void onDisconnect(final AppCoinsBillingStateListener listener) {
        logInfo("Billing Disconnected, notifying client onBillingServiceDisconnected.");
        service = null;
        isServiceReady = false;
        listener.onBillingServiceDisconnected();
    }

    @Override
    public PurchasesResult getPurchases(String skuType) throws ServiceConnectionException {
        logInfo("Executing getPurchases.");
        logInfo(String.format("Parameters skuType:%s", skuType));

        if (!isReady()) {
            logError("Service is not ready. Throwing ServiceConnectionException.");
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendServiceConnectionExceptionEvent(SdkGeneralFailureStep.GET_PURCHASES);
            throw new ServiceConnectionException();
        }
        try {
            Bundle purchases = service.getPurchases(apiVersion, packageName, skuType, null);
            logDebug("Purchases received: " + purchases.toString());

            PurchasesResult purchasesResult = AndroidBillingMapper.mapPurchases(purchases, skuType);
            logInfo("PurchasesResult code: " + purchasesResult.getResponseCode());

            return purchasesResult;
        } catch (RemoteException e) {
            logError("Error getting purchases. ", e);
            throw new ServiceConnectionException(e.getMessage());
        }
    }

    @Override
    public SkuDetailsResult querySkuDetailsAsync(final String skuType, final List<String> sku)
        throws ServiceConnectionException {
        logInfo("Executing querySkuDetailsAsync.");
        String size = "0";
        if (sku != null) {
            size = String.valueOf(sku.size());
        }
        logInfo(String.format("Parameters skuType:%s skuSize:%s", skuType, size));

        if (!isReady()) {
            logError("Service is not ready. Throwing ServiceConnectionException.");
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendServiceConnectionExceptionEvent(SdkGeneralFailureStep.QUERY_SKU_DETAILS);
            throw new ServiceConnectionException();
        }

        Bundle bundle = AndroidBillingMapper.mapArrayListToBundleSkuDetails(sku);
        logDebug("Sku Details bundle to request: " + bundle);

        SkuDetailsResult skuDetailsResult;

        try {
            do {
                Bundle response = service.getSkuDetails(apiVersion, packageName, skuType, bundle);
                logDebug("Sku Details received: " + response.toString());

                skuDetailsResult = AndroidBillingMapper.mapBundleToHashMapSkuDetails(skuType, response);

                if (skuDetailsResult.getResponseCode() == ResponseCode.SERVICE_UNAVAILABLE.getValue()) {
                    logError("Failed to get SkuDetails request: " + skuDetailsResult.getResponseCode());
                    Thread.sleep(5000);
                }
            } while (skuDetailsResult.getResponseCode() == ResponseCode.SERVICE_UNAVAILABLE.getValue());

            logInfo("SkuDetailsResult code: " + skuDetailsResult.getResponseCode());
            return skuDetailsResult;
        } catch (Exception e) {
            logError("Error querySkuDetailsAsync. ", e);
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendQuerySkuDetailsFailureParsingSkusEvent(sku, skuType);
            throw new ServiceConnectionException(e.getMessage());
        }
    }

    @Override
    public int consumeAsync(String purchaseToken) throws ServiceConnectionException {
        logInfo("Executing consumeAsync.");
        logDebug(String.format("Debuggable parameters purchaseToken:%s ", purchaseToken));

        if (!isReady()) {
            logError("Service is not ready. Throwing ServiceConnectionException.");
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendServiceConnectionExceptionEvent(SdkGeneralFailureStep.CONSUME);
            throw new ServiceConnectionException();
        }
        try {
            int consumeResult = service.consumePurchase(apiVersion, packageName, purchaseToken);
            logInfo("Consume result: " + consumeResult);

            return consumeResult;
        } catch (RemoteException e) {
            logError("Error consumeAsync. ", e);
            throw new ServiceConnectionException(e.getMessage());
        }
    }

    @Override
    public LaunchBillingFlowResult launchBillingFlow(String skuType, String sku, String payload, String oemid,
        String guestWalletId) throws ServiceConnectionException {
        logInfo("Executing launchBillingFlow.");
        logInfo(String.format("Parameters skuType:%s sku:%s oemid:%s", skuType, sku, oemid));
        logDebug(String.format("Debuggable parameters payload:%s guestWalletId:%s", payload, guestWalletId));

        if (!isReady()) {
            logError("Service is not ready. Throwing ServiceConnectionException.");
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendServiceConnectionExceptionEvent(SdkGeneralFailureStep.START_PURCHASE);
            throw new ServiceConnectionException();
        }
        try {
            Bundle response =
                service.getBuyIntent(apiVersion, packageName, sku, skuType, payload, oemid, guestWalletId);
            logDebug("Get Buy Intent bundle: " + response.toString());

            LaunchBillingFlowResult launchBillingFlowResult =
                AndroidBillingMapper.mapBundleToHashMapGetIntent(response);
            logInfo("LaunchBillingFlowResult code: " + launchBillingFlowResult.getResponseCode());
            return launchBillingFlowResult;
        } catch (RemoteException e) {
            logError("Error launchBillingFlow. ", e);
            throw new ServiceConnectionException(e.getMessage());
        }
    }

    @Override
    public boolean isReady() {
        return isServiceReady;
    }

    @Override
    public int isFeatureSupported(FeatureType feature) throws ServiceConnectionException {
        logInfo("Executing isFeatureSupported " + feature);

        if (!isReady()) {
            logError("Service is not ready. Throwing ServiceConnectionException.");
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendServiceConnectionExceptionEvent(SdkGeneralFailureStep.CONSUME);
            throw new ServiceConnectionException();
        }
        ResponseCode featureSupportedResult = IsFeatureSupported.INSTANCE.invoke(feature);
        logInfo("Feature supported result: " + featureSupportedResult);
        return featureSupportedResult.getValue();
    }
}
