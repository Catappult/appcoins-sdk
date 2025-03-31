package com.appcoins.sdk.billing.service

import android.os.Bundle
import android.os.IBinder
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.helpers.WalletUtils.startServiceUnavailableDialog
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod.Companion.getUnavailableBillingMessage
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logInfo
import java.io.Serializable

class UnavailableBillingService private constructor() : AppcoinsBilling, Serializable {

    init {
        unavailableBillingServiceInstance = this
    }

    override fun asBinder(): IBinder? = null

    override fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int =
        ResponseCode.SERVICE_UNAVAILABLE.value

    override fun getSkuDetails(
        apiVersion: Int,
        packageName: String,
        type: String,
        skusBundle: Bundle
    ): Bundle {
        logInfo("Getting SKU Details.")
        return buildErrorBundle()
    }

    override fun getBuyIntent(
        apiVersion: Int,
        packageName: String,
        sku: String,
        type: String,
        developerPayload: String?,
        oemid: String?,
        guestWalletId: String?,
    ): Bundle {
        logInfo("Getting Buy Intent. [apiVersion: $apiVersion | packageName: $packageName | sku: $sku | type: $type ]")
        logDebug(
            "Debuggable properties: [" +
                "developerPayload: $developerPayload | " +
                "oemid: $oemid | " +
                "guestWalletId: $guestWalletId ]"
        )
        return startServiceUnavailableDialog(
            getUnavailableBillingMessage(
                WalletUtils.paymentFlowMethods.toMutableList()
            )
        )
    }

    override fun getPurchases(
        apiVersion: Int,
        packageName: String,
        type: String,
        continuationToken: String?
    ): Bundle {
        logInfo("Getting Purchases of type: $type")
        return buildErrorBundle()
    }

    override fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int {
        logInfo("Consuming Purchase.")
        logDebug("Purchase Token: $purchaseToken")
        return ResponseCode.ERROR.value
    }

    private fun buildErrorBundle(): Bundle {
        val bundleResponse = Bundle()
        bundleResponse.putInt(RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.value)
        return bundleResponse
    }

    companion object {
        private var unavailableBillingServiceInstance: UnavailableBillingService? = null

        val instance: UnavailableBillingService?
            get() {
                if (unavailableBillingServiceInstance == null) {
                    unavailableBillingServiceInstance = UnavailableBillingService()
                }
                return unavailableBillingServiceInstance
            }
    }
}
