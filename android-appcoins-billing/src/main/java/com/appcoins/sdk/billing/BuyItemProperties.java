package com.appcoins.sdk.billing;

import java.io.Serializable;

public class BuyItemProperties implements Serializable {

    private final int apiVersion;
    private final String packageName;
    private final String sku;
    private final String type;
    private final DeveloperPayload developerPayload;
    private final SkuDetails skuDetails;

    public BuyItemProperties(int apiVersion, String packageName, String sku, String type,
                             DeveloperPayload developerPayload, SkuDetails skuDetails) {
        this.apiVersion = apiVersion;
        this.packageName = packageName;
        this.sku = sku;
        this.type = type;
        this.developerPayload = developerPayload;
        this.skuDetails = skuDetails;
    }

    public DeveloperPayload getDeveloperPayload() {
        return developerPayload;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSku() {
        return sku;
    }

    public String getType() {
        return type;
    }

    public SkuDetails getSkuDetails() {
        return skuDetails;
    }

    public BillingFlowParams toBillingFlowParams() {
        return new BillingFlowParams(
                sku,
                type,
                developerPayload.getOrderReference(),
                developerPayload.getDeveloperPayload(),
                developerPayload.getOrigin()
        );
    }
}
