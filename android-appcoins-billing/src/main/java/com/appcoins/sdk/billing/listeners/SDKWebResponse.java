package com.appcoins.sdk.billing.listeners;

public class SDKWebResponse {

    private final int resultCode;
    private final String data;

    public SDKWebResponse(int resultCode, String data) {
        this.resultCode = resultCode;
        this.data = data;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getData() {
        return data;
    }
}
