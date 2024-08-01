package com.appcoins.sdk.billing;

import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences;

import java.util.Random;

public class WalletInteract {

    private static int ID_LENGTH = 40;
    private AttributionSharedPreferences attributionSharedPreferences;

    public WalletInteract(AttributionSharedPreferences attributionSharedPreferences) {
        this.attributionSharedPreferences = attributionSharedPreferences;
    }

    public String retrieveWalletId() {
        String savedId = attributionSharedPreferences.getWalletId();
        if (savedId != null) {
            return savedId;
        } else {
            String generatedId = generateId();
            attributionSharedPreferences.setWalletId(generatedId);
            return generatedId;
        }
    }

    private String generateId() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < ID_LENGTH) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        sb.setLength(ID_LENGTH);
        return sb.toString();
    }
}
