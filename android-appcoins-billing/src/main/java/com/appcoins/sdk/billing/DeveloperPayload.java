package com.appcoins.sdk.billing;

import java.io.Serializable;

public class DeveloperPayload implements Serializable {

    private final String rawPayload;
    private final String developerPayload;
    private final String orderReference;
    private final String origin;
    private final String obfuscatedAccountId;
    private final Boolean freeTrial;

    public DeveloperPayload(String rawPayload, String developerPayload, String orderReference, String origin,
        String obfuscatedAccountId, Boolean freeTrial) {

        this.rawPayload = rawPayload;
        this.developerPayload = developerPayload;
        this.orderReference = orderReference;
        this.origin = origin;
        this.obfuscatedAccountId = obfuscatedAccountId;
        this.freeTrial = freeTrial;
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

    public Boolean getFreeTrial() {
        return freeTrial;
    }
}
