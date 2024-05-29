package com.appcoins.sdk.billing.helpers;

import android.os.IBinder;
import android.util.Log;

import com.appcoins.billing.AppcoinsBilling;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.communication.SyncIpcMessageRequester;
import com.appcoins.communication.requester.MessageRequesterFactory;
import com.appcoins.sdk.billing.UriCommunicationAppcoinsBilling;
import com.appcoins.sdk.billing.WalletBinderUtil;
import com.appcoins.sdk.billing.service.BdsService;
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences;

import java.io.Serializable;

public final class AppcoinsBillingStubHelper implements Serializable {
    private static final String TAG = AppcoinsBillingStubHelper.class.getSimpleName();
    private static AppcoinsBillingStubHelper appcoinsBillingStubHelper;

    private AppcoinsBillingStubHelper() {
        appcoinsBillingStubHelper = this;
    }

    public static AppcoinsBillingStubHelper getInstance() {
        if (appcoinsBillingStubHelper == null) {
            appcoinsBillingStubHelper = new AppcoinsBillingStubHelper();
        }
        return appcoinsBillingStubHelper;
    }

    public static abstract class Stub {
        public static AppcoinsBilling asInterface(IBinder service) {
            Log.i(TAG, "Stub: BindType " + WalletBinderUtil.getBindType() + ", service " + service);

            if (WalletBinderUtil.getBindType() == BindType.BILLING_SERVICE_NOT_INSTALLED) {
                return WebAppcoinsBilling.Companion.getInstance();
            } else {
                AttributionSharedPreferences attributionSharedPreferences =
                        new AttributionSharedPreferences(WalletUtils.getContext());
                AppcoinsBilling appcoinsBilling;
                if (WalletBinderUtil.getBindType() == BindType.URI_CONNECTION) {
                    SyncIpcMessageRequester messageRequester =
                            MessageRequesterFactory.create(WalletUtils.getLifecycleActivityProvider(),
                                    BuildConfig.APPCOINS_WALLET_PACKAGE_NAME,
                                    "appcoins://billing/communication/processor/1",
                                    "appcoins://billing/communication/requester/1", BdsService.TIME_OUT_IN_MILLIS);
                    appcoinsBilling = new UriCommunicationAppcoinsBilling(messageRequester);
                } else {
                    appcoinsBilling = AppcoinsBilling.Stub.asInterface(service);
                }
                return new AppcoinsBillingWrapper(appcoinsBilling,
                        AppCoinsPendingIntentCaller.getInstance(), attributionSharedPreferences.getWalletId(),
                        BdsService.TIME_OUT_IN_MILLIS);
            }
        }
    }
}