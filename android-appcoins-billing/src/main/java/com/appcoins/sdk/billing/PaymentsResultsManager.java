package com.appcoins.sdk.billing;

import android.util.Log;

import com.appcoins.sdk.billing.listeners.SDKWebResponse;
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream;

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
        SDKWebResponseStream.getInstance().collectFromNow(sdkWebResponseCollector);
    }

    private final SDKWebResponseStream.Consumer<SDKWebResponse> sdkWebResponseCollector =
            sdkWebResponse -> {
                Log.i("CatappultAppcoins", "collectFromNow: received new sdkWebResponse -> " + sdkWebResponse.toString());
                ApplicationUtils.handleActivityResult(
                        catapultAppcoinsBilling.getBilling(),
                        sdkWebResponse.getResultCode(),
                        null, //new Intent(sdkWebResponse.getData()),
                        catapultAppcoinsBilling.getPurchaseFinishedListener());
            };
}
