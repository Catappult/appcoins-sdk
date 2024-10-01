package com.appcoins.sdk.billing.analytics;

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsManagerProvider {

    private static AnalyticsManager analyticsManagerInstance = null;

    public static AnalyticsManager provideAnalyticsManager() {
        if (analyticsManagerInstance == null) {
            IndicativeEventLogger indicativeEventLogger = new IndicativeEventLogger();

            analyticsManagerInstance =
                    new AnalyticsManager.Builder()
                            .addLogger(indicativeEventLogger, provideIndicativeEventList())
                            .setAnalyticsNormalizer(new KeysNormalizer())
                            .build();
        }
        return analyticsManagerInstance;
    }

    private static List<String> provideIndicativeEventList() {
        List<String> list = new ArrayList<>();
        list.add(SdkAnalyticsEvents.SDK_START_CONNECTION);
        list.add(SdkAnalyticsEvents.SDK_IAP_PURCHASE_INTENT_START);
        list.add(SdkAnalyticsEvents.SDK_IAP_PAYMENT_STATUS_FEEDBACK);
        list.add(SdkAnalyticsEvents.SDK_WEB_PAYMENT_IMPRESSION);
        list.add(SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE);
        list.add(SdkInstallFlowEvents.SDK_WALLET_INSTALL_IMPRESSION);
        list.add(SdkInstallFlowEvents.SDK_WALLET_INSTALL_CLICK);
        list.add(SdkInstallFlowEvents.SDK_INSTALL_WALLET_FEEDBACK);
        list.add(SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION);
        list.add(SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION);
        list.add(SdkUpdateFlowEvents.SDK_APP_UPDATE_DEEPLINK_IMPRESSION);
        list.add(SdkUpdateFlowEvents.SDK_APP_UPDATE_IMPRESSION);
        list.add(SdkUpdateFlowEvents.SDK_APP_UPDATE_CLICK);
        list.add(SdkBackendPayflowEvents.SDK_CALL_BACKEND_PAYFLOW);
        list.add(SdkBackendPayflowEvents.SDK_CALL_BACKEND_WEB_PAYMENT_URL);
        list.add(SdkBackendPayflowEvents.SDK_CALL_BACKEND_ATTRIBUTION);
        list.add(SdkBackendPayflowEvents.SDK_CALL_BACKEND_APP_VERSION);
        list.add(SdkBackendPayflowEvents.SDK_CALL_BACKEND_STORE_LINK);
        list.add(SdkBackendPayflowEvents.SDK_CALL_BINDSERVICE_ATTEMPT);
        list.add(SdkBackendPayflowEvents.SDK_CALL_BINDSERVICE_FAIL);
        return list;
    }
}
