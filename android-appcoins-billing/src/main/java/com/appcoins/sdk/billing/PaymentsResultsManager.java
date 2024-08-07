package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.listeners.SDKWebResponse;
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream;

class PaymentsResultsManager {

    private BillingFlowParams billingFlowParams = null;
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

    public void collectPaymentResult(BillingFlowParams billingFlowParams, CatapultAppcoinsBilling catapultAppcoinsBilling) {
        this.billingFlowParams = billingFlowParams;
        this.catapultAppcoinsBilling = catapultAppcoinsBilling;
        SDKWebResponseStream.getInstance().collect(sdkWebResponseCollector);
    }

    private final SDKWebResponseStream.Consumer<SDKWebResponse> sdkWebResponseCollector =
            sdkWebResponse -> {
                ApplicationUtils.handleWebBasedResult(
                        sdkWebResponse,
                        billingFlowParams,
                        catapultAppcoinsBilling.getPurchaseFinishedListener());
            };
}
