package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.listeners.SDKWebResponse;
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream;

class PaymentsResultsManager {

    private BillingFlowParams billingFlowParams = null;
    private String developerPayload = null;
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

    public void collectPaymentResult(BillingFlowParams billingFlowParams, String developerPayload, CatapultAppcoinsBilling catapultAppcoinsBilling) {
        this.billingFlowParams = billingFlowParams;
        this.developerPayload = developerPayload;
        this.catapultAppcoinsBilling = catapultAppcoinsBilling;
        SDKWebResponseStream.getInstance().collect(sdkWebResponseCollector);
    }

    private final SDKWebResponseStream.Consumer<SDKWebResponse> sdkWebResponseCollector =
            sdkWebResponse -> {
                ApplicationUtils.handleWebBasedResult(
                        sdkWebResponse,
                        // TODO Inject correct data for associated purchase
                            // 1 - Web send full data from Purchase
                                // Dependency on Wallet
                            // 2 - Request data from an endpoint via PurchaseToken/OrderId
                                // Adds delay to the response
                        billingFlowParams,
                        developerPayload,
                        catapultAppcoinsBilling.getPurchaseFinishedListener());
            };
}
