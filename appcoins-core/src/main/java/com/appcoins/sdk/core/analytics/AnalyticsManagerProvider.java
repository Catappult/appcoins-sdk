package com.appcoins.sdk.core.analytics;

import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableEvents;
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents;
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseEvents;
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureEvents;
import com.appcoins.sdk.core.analytics.events.SdkGetReferralDeeplinkEvents;
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents;
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogEvents;
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateDialogEvents;
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateEvents;
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateStoreEvents;
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents;
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesEvents;
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsEvents;
import com.appcoins.sdk.core.analytics.events.SdkWalletPaymentFlowEvents;
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents;
import com.appcoins.sdk.core.analytics.indicative.IndicativeEventLogger;
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsManagerProvider {

    private static AnalyticsManager analyticsManagerInstance = null;

    public static AnalyticsManager provideAnalyticsManager() {
        if (analyticsManagerInstance == null) {
            IndicativeEventLogger indicativeEventLogger = new IndicativeEventLogger();

            analyticsManagerInstance =
                new AnalyticsManager.Builder().addLogger(indicativeEventLogger, provideIndicativeEventList())
                    .setAnalyticsNormalizer(new KeysNormalizer())
                    .build();
        }
        return analyticsManagerInstance;
    }

    private static List<String> provideIndicativeEventList() {
        List<String> list = new ArrayList<>();
        list.add(SdkAppUpdateAvailableEvents.SDK_APP_UPDATE_AVAILABLE_REQUEST);
        list.add(SdkAppUpdateAvailableEvents.SDK_APP_UPDATE_AVAILABLE_RESULT);
        list.add(SdkAppUpdateAvailableEvents.SDK_APP_UPDATE_AVAILABLE_MAIN_THREAD_FAILURE);
        list.add(SdkAppUpdateAvailableEvents.SDK_APP_UPDATE_AVAILABLE_FAILURE_TO_OBTAIN_RESULT);
        list.add(SdkAppUpdateAvailableEvents.SDK_APP_UPDATE_AVAILABLE_FAILURE_TO_OBTAIN_RESULT);
        list.add(SdkBackendRequestEvents.SDK_CALL_BACKEND_REQUEST);
        list.add(SdkBackendRequestEvents.SDK_CALL_BACKEND_RESPONSE);
        list.add(SdkBackendRequestEvents.SDK_CALL_BACKEND_MAPPING_FAILURE);
        list.add(SdkBackendRequestEvents.SDK_CALL_BACKEND_ERROR);
        list.add(SdkConsumePurchaseEvents.SDK_CONSUME_PURCHASE_REQUEST);
        list.add(SdkConsumePurchaseEvents.SDK_CONSUME_PURCHASE_RESULT);
        list.add(SdkGeneralFailureEvents.SDK_UNEXPECTED_FAILURE);
        list.add(SdkGeneralFailureEvents.SDK_PURCHASE_SIGNATURE_VERIFICATION_FAILURE);
        list.add(SdkGeneralFailureEvents.SDK_SERVICE_CONNECTION_EXCEPTION);
        list.add(SdkGetReferralDeeplinkEvents.SDK_GET_REFERRAL_DEEPLINK_REQUEST);
        list.add(SdkGetReferralDeeplinkEvents.SDK_GET_REFERRAL_DEEPLINK_RESULT);
        list.add(SdkGetReferralDeeplinkEvents.SDK_GET_REFERRAL_DEEPLINK_MAIN_THREAD_FAILURE);
        list.add(SdkInitializationEvents.SDK_ATTRIBUTION_REQUEST);
        list.add(SdkInitializationEvents.SDK_ATTRIBUTION_RESULT);
        list.add(SdkInitializationEvents.SDK_ATTRIBUTION_REQUEST_FAILURE);
        list.add(SdkInitializationEvents.SDK_ATTRIBUTION_RETRY_ATTEMPT);
        list.add(SdkInitializationEvents.SDK_PAYFLOW_REQUEST);
        list.add(SdkInitializationEvents.SDK_PAYFLOW_RESULT);
        list.add(SdkInitializationEvents.SDK_START_CONNECTION);
        list.add(SdkInitializationEvents.SDK_SERVICE_CONNECTED);
        list.add(SdkInitializationEvents.SDK_SERVICE_CONNECTION_FAILED);
        list.add(SdkInitializationEvents.SDK_FINISH_CONNECTION);
        list.add(SdkInitializationEvents.SDK_APP_INSTALLATION_TRIGGER);
        list.add(SdkInstallWalletDialogEvents.SDK_INSTALL_WALLET_DIALOG);
        list.add(SdkInstallWalletDialogEvents.SDK_INSTALL_WALLET_DIALOG_ACTION);
        list.add(SdkInstallWalletDialogEvents.SDK_INSTALL_WALLET_DIALOG_DOWNLOAD_WALLET_VANILLA);
        list.add(SdkInstallWalletDialogEvents.SDK_INSTALL_WALLET_DIALOG_DOWNLOAD_WALLET_FALLBACK);
        list.add(SdkInstallWalletDialogEvents.SDK_INSTALL_WALLET_DIALOG_SUCCESS);
        list.add(SdkLaunchAppUpdateDialogEvents.SDK_LAUNCH_APP_UPDATE_DIALOG_REQUEST);
        list.add(SdkLaunchAppUpdateDialogEvents.SDK_LAUNCH_APP_UPDATE_DIALOG_ACTION);
        list.add(SdkLaunchAppUpdateStoreEvents.SDK_LAUNCH_APP_UPDATE_STORE_REQUEST);
        list.add(SdkLaunchAppUpdateEvents.SDK_LAUNCH_APP_UPDATE_RESULT);
        list.add(SdkLaunchAppUpdateEvents.SDK_LAUNCH_APP_UPDATE_DEEPLINK_FAILURE);
        list.add(SdkPurchaseFlowEvents.SDK_LAUNCH_PURCHASE);
        list.add(SdkPurchaseFlowEvents.SDK_PURCHASE_RESULT);
        list.add(SdkPurchaseFlowEvents.SDK_LAUNCH_PURCHASE_MAIN_THREAD_FAILURE);
        list.add(SdkPurchaseFlowEvents.SDK_LAUNCH_PURCHASE_TYPE_NOT_SUPPORTED_FAILURE);
        list.add(SdkQueryPurchasesEvents.SDK_QUERY_PURCHASES_REQUEST);
        list.add(SdkQueryPurchasesEvents.SDK_QUERY_PURCHASES_RESULT);
        list.add(SdkQueryPurchasesEvents.SDK_QUERY_PURCHASES_TYPE_NOT_SUPPORTED_ERROR);
        list.add(SdkQuerySkuDetailsEvents.SDK_QUERY_SKU_DETAILS_REQUEST);
        list.add(SdkQuerySkuDetailsEvents.SDK_QUERY_SKU_DETAILS_RESULT);
        list.add(SdkQuerySkuDetailsEvents.SDK_QUERY_SKU_DETAILS_FAILURE_PARSING_SKUS);
        list.add(SdkWalletPaymentFlowEvents.SDK_WALLET_PAYMENT_START);
        list.add(SdkWalletPaymentFlowEvents.SDK_WALLET_PAYMENT_EMPTY_DATA);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_START);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_OPEN_DEEPLINK);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_FAILURE_TO_OPEN_DEEPLINK);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_FAILURE_TO_OBTAIN_URL);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_ALLOW_EXTERNAL_APPS);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_LAUNCH_EXTERNAL_PAYMENT);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_EXTERNAL_PAYMENT_RESULT);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_WALLET_PAYMENT_RESULT);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_EXECUTE_EXTERNAL_DEEPLINK);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_ERROR_PROCESSING_PURCHASE_RESULT);
        list.add(SdkWebPaymentFlowEvents.SDK_WEB_PAYMENT_PURCHASE_RESULT_EMPTY);
        return list;
    }
}
