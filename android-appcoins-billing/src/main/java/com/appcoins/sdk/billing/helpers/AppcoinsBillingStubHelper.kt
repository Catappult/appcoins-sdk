package com.appcoins.sdk.billing.helpers

import android.os.IBinder
import android.util.Log
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.communication.requester.MessageRequesterFactory
import com.appcoins.sdk.billing.UriCommunicationAppcoinsBilling
import com.appcoins.sdk.billing.WalletBinderUtil.bindType
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences

object AppcoinsBillingStubHelper {
    private val TAG = AppcoinsBillingStubHelper::class.java.simpleName

    object Stub {
        @JvmStatic
        fun asInterface(service: IBinder): AppcoinsBilling? {
            Log.i(TAG, "Stub: BindType $bindType, service $service")

            if (bindType == BindType.BILLING_SERVICE_NOT_INSTALLED) {
                return WebAppcoinsBilling.instance
            } else {
                val attributionSharedPreferences =
                    AttributionSharedPreferences(WalletUtils.getContext())
                val appcoinsBilling: AppcoinsBilling
                if (bindType == BindType.URI_CONNECTION) {
                    val messageRequester =
                        MessageRequesterFactory.create(
                            WalletUtils.getLifecycleActivityProvider(),
                            BuildConfig.APPCOINS_WALLET_PACKAGE_NAME,
                            "appcoins://billing/communication/processor/1",
                            "appcoins://billing/communication/requester/1",
                            BdsService.TIME_OUT_IN_MILLIS
                        )
                    appcoinsBilling = UriCommunicationAppcoinsBilling(messageRequester)
                } else {
                    appcoinsBilling = AppcoinsBilling.Stub.asInterface(service)
                }
                return AppcoinsBillingWrapper(
                    appcoinsBilling,
                    AppCoinsPendingIntentCaller.getInstance(),
                    attributionSharedPreferences.getWalletId(),
                    BdsService.TIME_OUT_IN_MILLIS
                )
            }
        }
    }
}