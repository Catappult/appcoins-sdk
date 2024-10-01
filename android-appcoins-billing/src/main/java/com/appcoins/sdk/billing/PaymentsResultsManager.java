package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.listeners.PaymentResponseStream;
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse;

class PaymentsResultsManager {

    private CatapultAppcoinsBilling catapultAppcoinsBilling = null;
    private static PaymentsResultsManager instance;

    private PaymentsResultsManager() {
    }

    public static synchronized PaymentsResultsManager getInstance() {
        if (instance == null) {
            instance = new PaymentsResultsManager();
        }
        return instance;
    }

    public void collectPaymentResult(CatapultAppcoinsBilling catapultAppcoinsBilling) {
        this.catapultAppcoinsBilling = catapultAppcoinsBilling;
        PaymentResponseStream.getInstance()
            .collect(sdkWebResponseCollector);
    }

    private final PaymentResponseStream.Consumer<SDKPaymentResponse> sdkWebResponseCollector =
        sdkWebResponse -> ApplicationUtils.handleActivityResult(
            catapultAppcoinsBilling.getBilling(), sdkWebResponse.getResultCode(),
            sdkWebResponse.getIntent(), catapultAppcoinsBilling.getPurchaseFinishedListener());
}
