package com.appcoins.sdk.billing.helpers

import android.os.IBinder
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.communication.requester.MessageRequesterFactory
import com.appcoins.sdk.billing.UriCommunicationAppcoinsBilling
import com.appcoins.sdk.billing.WalletBinderUtil.bindType
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.UnavailableBillingService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.webpayment.WebAppcoinsBilling
import com.appcoins.sdk.core.logger.Logger.logInfo

object AppcoinsBillingStubHelper {

    object Stub {
        @JvmStatic
        fun asInterface(service: IBinder): AppcoinsBilling? {
            logInfo("Stub: BindType $bindType, service $service")
            return when (bindType) {
                BindType.BILLING_SERVICE_NOT_INSTALLED -> {
                    logInfo("AppcoinsBilling of type WebAppcoinsBilling.")
                    WebAppcoinsBilling.instance
                }

                BindType.UNAVAILABLE_BILLING -> {
                    logInfo("AppcoinsBilling of type UnavailableBillingService.")
                    UnavailableBillingService.instance
                }

                else -> {
                    val attributionSharedPreferences =
                        AttributionSharedPreferences(WalletUtils.context)
                    val appcoinsBilling: AppcoinsBilling
                    if (bindType == BindType.URI_CONNECTION) {
                        logInfo("AppcoinsBilling of type UriCommunicationAppcoinsBilling.")
                        val messageRequester =
                            MessageRequesterFactory.create(
                                WalletUtils.context,
                                BuildConfig.APPCOINS_WALLET_PACKAGE_NAME,
                                "appcoins://billing/communication/processor/1",
                                "appcoins://billing/communication/requester/1",
                                BdsService.TIME_OUT_IN_MILLIS
                            )
                        appcoinsBilling = UriCommunicationAppcoinsBilling(messageRequester)
                    } else {
                        logInfo("AppcoinsBilling of type WalletBillingService.")
                        appcoinsBilling = AppcoinsBilling.Stub.asInterface(service)
                    }
                    AppcoinsBillingWrapper(appcoinsBilling, attributionSharedPreferences.getWalletId())
                }
            }
        }
    }
}
