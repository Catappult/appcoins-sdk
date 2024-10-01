package com.appcoins.sdk.billing.analytics;

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager;
import java.util.HashMap;
import java.util.Map;

public class BillingAnalytics implements EventSender {
    public static final String START_INSTALL = "appcoins_guest_sdk_install_wallet";
    static final String PAYMENT_START = "appcoins_guest_sdk_payment_start";
    private static final String SDK = "AppCoinsGuestSDK";
    private static final String EVENT_PACKAGE_NAME = "package_name";
    private static final String EVENT_SKU = "sku";
    private static final String EVENT_VALUE = "value";
    private static final String EVENT_TRANSACTION_TYPE = "transaction_type";
    private static final String EVENT_CONTEXT = "context";
    private final AnalyticsManager analytics;

    public BillingAnalytics(AnalyticsManager analytics) {
        this.analytics = analytics;
    }

    @Override
    public void sendPurchaseStartEvent(String packageName, String skuDetails, String value,
        String transactionType, String context) {
        Map<String, Object> eventData = new HashMap<>();

        eventData.put(EVENT_PACKAGE_NAME, packageName);
        eventData.put(EVENT_SKU, skuDetails);
        eventData.put(EVENT_VALUE, value);
        eventData.put(EVENT_TRANSACTION_TYPE, transactionType);
        eventData.put(EVENT_CONTEXT, context);

        analytics.logEvent(eventData, PAYMENT_START, AnalyticsManager.Action.CLICK, SDK);
    }
}
