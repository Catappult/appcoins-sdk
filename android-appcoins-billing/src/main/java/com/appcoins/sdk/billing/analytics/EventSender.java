package com.appcoins.sdk.billing.analytics;

interface EventSender {

    void sendPurchaseStartEvent(String packageName, String skuDetails, String value, String transactionType,
        String context);
}
