package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.listeners.PurchaseData
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.billing.listeners.WalletPaymentDeeplinkResponseStream
import com.appcoins.sdk.billing.managers.ProductV2Manager
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

object HandlePurchaseResultFromWalletDeeplink : UseCase() {
    operator fun invoke(responseCode: Int, purchaseToken: String?) {
        super.invokeUseCase()
        Thread {
            if (responseCode == ResponseCode.OK.value && !purchaseToken.isNullOrEmpty()) {
                handleSuccessResult(responseCode, purchaseToken)
            } else {
                handleFailureResult(responseCode)
            }
        }.start()
    }

    private fun handleSuccessResult(responseCode: Int, purchaseToken: String) {
        try {
            val attributionSharedPreferences =
                AttributionSharedPreferences(WalletUtils.getContext())
            val walletId = attributionSharedPreferences.getWalletId()

            val purchaseResponse =
                ProductV2Manager.getPurchaseSync(
                    WalletUtils.getContext().packageName,
                    walletId,
                    purchaseToken
                )

            requireNotNull(purchaseResponse)
            requireNotNull(purchaseResponse.purchase)

            WalletPaymentDeeplinkResponseStream.getInstance().emit(
                SDKWebResponse(
                    responseCode,
                    PurchaseData(JSONObject(purchaseResponse.purchase.verification.data)),
                    purchaseResponse.purchase.verification.signature,
                    null,
                )
            )
        } catch (e: Exception) {
            logError("There was a failure parsing the Purchase Result from the Wallet Deeplink.", e)
            WalletPaymentDeeplinkResponseStream.getInstance().emit(SDKWebResponse(ResponseCode.ERROR.value))
        }
    }

    private fun handleFailureResult(responseCode: Int) {
        WalletPaymentDeeplinkResponseStream.getInstance().emit(SDKWebResponse(responseCode))
    }
}
