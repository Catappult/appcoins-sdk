package com.appcoins.sdk.billing;

import java.io.Serializable;

public class DeveloperPayload implements Serializable {

    private final String rawPayload;
    private final String developerPayload;
    private final String orderReference;
    private final String origin;
    private final String obfuscatedAccountId;

    public DeveloperPayload(String rawPayload, String developerPayload, String orderReference, String origin,
        String obfuscatedAccountId) {

        this.rawPayload = rawPayload;
        this.developerPayload = developerPayload;
        this.orderReference = orderReference;
        this.origin = origin;
        this.obfuscatedAccountId = obfuscatedAccountId;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public String getOrigin() {
        return origin;
    }

    public String getObfuscatedAccountId() {
        return obfuscatedAccountId;
    }
}
