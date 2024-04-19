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
            SDKWebResponseStream.getInstance().emit(new SDKWebResponse(uri));
        }

        finish();
    }
}