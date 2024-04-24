package com.appcoins.sdk.billing;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.appcoins.sdk.billing.listeners.SDKWebResponse;
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream;

public class WebIapCommunicationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null) {
            SDKWebResponse sdkWebResponse = new SDKWebResponse(uri);
            saveGuestWalletId(uri);
            SDKWebResponseStream.getInstance().emit(sdkWebResponse);
        }

        finish();
    }

    private void saveGuestWalletId(Uri uri) {
        String guestWalletId = uri.getQueryParameter(GUEST_WALLET_ID_KEY);
        if (guestWalletId == null || guestWalletId.isEmpty()) {
            return;
        }

        SharedPreferencesRepository sharedPreferencesRepository =
                new SharedPreferencesRepository(
                        this,
                        SharedPreferencesRepository.TTL_IN_SECONDS
                );
        sharedPreferencesRepository.setWalletId(guestWalletId);
    }

    private static final String GUEST_WALLET_ID_KEY = "guestWalletID";
}