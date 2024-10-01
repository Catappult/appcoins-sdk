package com.asf.appcoins.toolbox;

import android.app.Application;

import com.appcoins.sdk.billing.helpers.WalletUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        WalletUtils.initIap(this);
    }
}
