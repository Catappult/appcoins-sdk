package com.appcoins.sdk.billing

import android.app.Activity
import android.content.Intent
import android.util.Base64
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.usecases.mmp.SendSuccessfulPurchaseResponseEvent
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

internal object ApplicationUtils {

    @JvmStatic
    fun handleActivityResult(
        billing: Billing,
        resultCode: Int,
        data: Intent?,
        purchaseFinishedListener: PurchasesUpdatedListener
    ) {
        val sdkAnalytics = WalletUtils.getSdkAnalytics()

        if (data == null) {
            logError("Null data in IAB activity result.")
            purchaseFinishedListener.onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
            return
        }

        val responseCode = getResponseCodeFromIntent(data)
        val purchaseData = data.getStringExtra(INAPP_PURCHASE_DATA)
        val dataSignature = data.getStringExtra(INAPP_DATA_SIGNATURE)

        if (resultCode == Activity.RESULT_OK && responseCode == ResponseCode.OK.value) {
            sdkAnalytics.sendPurchaseStatusEvent("success", getResponseDesc(responseCode))
            logDebug("Successful resultcode from purchase activity.")
            logDebug("Purchase data: $purchaseData")
            logDebug("Data signature: $dataSignature")
            logDebug("Extras: " + data.extras)

            if (purchaseData == null || dataSignature == null) {
                logError("BUG: either purchaseData or dataSignature is null.")
                logDebug("Extras: " + data.extras)
                purchaseFinishedListener.onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
                return
            }

            if (
                billing.verifyPurchase(purchaseData, Base64.decode(dataSignature, Base64.DEFAULT))
            ) {
                val purchaseDataJSON: JSONObject
                try {
                    purchaseDataJSON = JSONObject(purchaseData)
                    val purchase =
                        Purchase(
                            getObjectFromJson(purchaseDataJSON, "orderId"),
                            "inapp",
                            purchaseData,
                            Base64.decode(dataSignature, Base64.DEFAULT),
                            getObjectFromJson(purchaseDataJSON, "purchaseTime").toLong(),
                            Integer.decode(getObjectFromJson(purchaseDataJSON, "purchaseState")),
                            getObjectFromJson(purchaseDataJSON, "developerPayload"),
                            getObjectFromJson(purchaseDataJSON, "purchaseToken"),
                            getObjectFromJson(purchaseDataJSON, "packageName"),
                            getObjectFromJson(purchaseDataJSON, "productId"),
                            getObjectFromJson(purchaseDataJSON, "isAutoRenewing").toBoolean()
                        )

                    val purchases: MutableList<Purchase> = ArrayList()
                    purchases.add(purchase)
                    SendSuccessfulPurchaseResponseEvent.invoke(purchase)
                    purchaseFinishedListener.onPurchasesUpdated(responseCode, purchases)
                } catch (e: Exception) {
                    e.printStackTrace()
                    purchaseFinishedListener
                        .onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
                    logError("Failed to parse purchase data.")
                }
            } else {
                logError("Signature verification failed for sku:")
                purchaseFinishedListener.onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
            }
        } else if (resultCode == Activity.RESULT_OK) {
            // result code was OK, but in-app billing response was not OK.
            logDebug(
                "Result code was OK but in-app billing response was not OK: " +
                        getResponseDesc(responseCode)
            )
            sdkAnalytics.sendPurchaseStatusEvent("error", getResponseDesc(responseCode))
            purchaseFinishedListener.onPurchasesUpdated(responseCode, emptyList())
        } else if (resultCode == Activity.RESULT_CANCELED) {
            logDebug("Purchase canceled - Response: " + getResponseDesc(responseCode))
            sdkAnalytics.sendPurchaseStatusEvent("user_canceled", getResponseDesc(responseCode))
            purchaseFinishedListener
                .onPurchasesUpdated(ResponseCode.USER_CANCELED.value, emptyList())
        } else {
            logError(
                "Purchase failed. Result code: $resultCode. Response: " +
                        getResponseDesc(responseCode)
            )
            sdkAnalytics.sendPurchaseStatusEvent("error", getResponseDesc(responseCode))
            purchaseFinishedListener.onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
        }
    }

    private fun getResponseCodeFromIntent(intent: Intent): Int =
        intent.getIntExtra(RESPONSE_CODE, ResponseCode.ERROR.value)

    private fun getObjectFromJson(data: JSONObject, objectId: String): String =
        data.optString(objectId)

    private fun getResponseDesc(code: Int): String {
        val iabMsgs = ("0:OK/1:User Canceled/2:Unknown/"
                + "3:Billing Unavailable/4:Item unavailable/"
                + "5:Developer Error/6:Error/7:Item Already Owned/"
                + "8:Item not owned").split("/".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val iabHelperMsgs = ("0:OK/-1001:Remote exception during initialization/"
                + "-1002:Bad response received/"
                + "-1003:Purchase signature verification failed/"
                + "-1004:Send intent failed/"
                + "-1005:User cancelled/"
                + "-1006:Unknown purchase response/"
                + "-1007:Missing token/"
                + "-1008:Unknown error/"
                + "-1009:Subscriptions not available/"
                + "-1010:Invalid consumption attempt").split("/".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()

        return if (code <= -1000) {
            val index = -1000 - code
            if (index < iabHelperMsgs.size) {
                iabHelperMsgs[index]
            } else {
                "$code:Unknown IAB Helper Error"
            }
        } else if (code < 0 || code >= iabMsgs.size) {
            "$code:Unknown"
        } else {
            iabMsgs[code]
        }
    }
}
