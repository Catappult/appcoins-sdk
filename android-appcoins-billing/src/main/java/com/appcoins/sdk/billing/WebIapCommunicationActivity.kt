package com.appcoins.sdk.billing;

import static com.appcoins.sdk.core.logger.Logger.logInfo;

import android.app.Activity;
import android.os.Bundle;

public class WebIapCommunicationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logInfo("Deeplink to SDK requested.");
        finish();
    }
}