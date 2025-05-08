package com.appcoins.sdk.billing

import android.app.Activity
import android.content.Intent
import android.util.Base64
import com.appcoins.sdk.billing.usecases.mmp.SendSuccessfulPurchaseResponseEvent
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.SKU_TYPE
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import org.json.JSONObject

internal object ApplicationUtils {

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    @JvmStatic
    fun handleActivityResult(
        billing: Billing,
        resultCode: Int,
        data: Intent?,
        purchaseFinishedListener: PurchasesUpdatedListener
    ) {
        val sdkAnalytics = SdkAnalyticsUtils.sdkAnalytics

        if (data == null) {
            logError("Null data in IAB activity result.")
            sdkAnalytics.sendPurchaseResultEvent(
                responseCode = ResponseCode.ERROR.value,
                failureMessage = "Null data in IAB activity result."
            )
            purchaseFinishedListener.onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
            return
        }

        val responseCode = getResponseCodeFromIntent(data)
        val purchaseData = data.getStringExtra(INAPP_PURCHASE_DATA)
        val dataSignature = data.getStringExtra(INAPP_DATA_SIGNATURE)
        val skuType = data.getStringExtra(SKU_TYPE)

        if (resultCode == Activity.RESULT_OK && responseCode == ResponseCode.OK.value) {
            logInfo("Successful ResultCode from Purchase.")
            logDebug("Purchase data: $purchaseData")
            logDebug("Data signature: $dataSignature")
            logDebug("Extras: " + data.extras)

            if (purchaseData == null || dataSignature == null) {
                logError("BUG: either purchaseData or dataSignature is null.")
                logDebug("Extras: " + data.extras)
                sdkAnalytics.sendPurchaseResultEvent(
                    responseCode = ResponseCode.ERROR.value,
                    failureMessage = "Either purchaseData or dataSignature is null."
                )
                purchaseFinishedListener.onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
                return
            }

            if (billing.verifyPurchase(purchaseData, Base64.decode(dataSignature, Base64.DEFAULT))) {
                val purchaseDataJSON: JSONObject
                try {
                    purchaseDataJSON = JSONObject(purchaseData)
                    val purchase =
                        Purchase(
                            getObjectFromJson(purchaseDataJSON, "orderId"),
                            skuType ?: "inapp",
                            purchaseData,
                            Base64.decode(dataSignature, Base64.DEFAULT),
                            getObjectFromJson(purchaseDataJSON, "purchaseTime").toLong(),
                            Integer.decode(getObjectFromJson(purchaseDataJSON, "purchaseState")),
                            getObjectFromJson(purchaseDataJSON, "developerPayload"),
                            getObjectFromJson(purchaseDataJSON, "obfuscatedAccountId"),
                            getObjectFromJson(purchaseDataJSON, "purchaseToken"),
                            getObjectFromJson(purchaseDataJSON, "packageName"),
                            getObjectFromJson(purchaseDataJSON, "productId"),
                            getObjectFromJson(purchaseDataJSON, "isAutoRenewing").toBoolean()
                        )

                    val purchases: MutableList<Purchase> = ArrayList()
                    purchases.add(purchase)
                    SendSuccessfulPurchaseResponseEvent.invoke(purchase)
                    sdkAnalytics.sendPurchaseResultEvent(responseCode, purchase.token, purchase.sku)
                    purchaseFinishedListener.onPurchasesUpdated(responseCode, purchases)
                    logInfo("Purchase result successfully sent.")
                } catch (e: Exception) {
                    logError("Failed to parse purchase data: $e")
                    sdkAnalytics.sendPurchaseResultEvent(
                        responseCode = ResponseCode.ERROR.value,
                        failureMessage = "Purchase failed. Result code: $resultCode."
                    )
                    purchaseFinishedListener
                        .onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
                }
            } else {
                logError("Signature verification failed.")
                sdkAnalytics.sendPurchaseResultEvent(
                    responseCode = ResponseCode.ERROR.value,
                    failureMessage = "Signature verification failed."
                )
                purchaseFinishedListener.onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
            }
        } else if (resultCode == Activity.RESULT_OK) {
            // result code was OK, but in-app billing response was not OK.
            logError(
                "Result code was OK but in-app billing response was not OK: " +
                    getResponseDesc(responseCode)
            )
            logDebug("Bundle: $data")
            sdkAnalytics.sendPurchaseResultEvent(
                responseCode = responseCode,
                failureMessage = "Result code was OK but in-app billing response was not OK."
            )
            purchaseFinishedListener.onPurchasesUpdated(responseCode, emptyList())
        } else if (resultCode == Activity.RESULT_CANCELED) {
            logInfo("Purchase canceled - Response: " + getResponseDesc(responseCode))
            logDebug("Bundle: $data")
            sdkAnalytics.sendPurchaseResultEvent(responseCode = ResponseCode.USER_CANCELED.value)
            purchaseFinishedListener.onPurchasesUpdated(ResponseCode.USER_CANCELED.value, emptyList())
        } else {
            logError(
                "Purchase failed. Result code: $resultCode. Response: " +
                    getResponseDesc(responseCode)
            )
            logDebug("Bundle: $data")
            sdkAnalytics.sendPurchaseResultEvent(
                responseCode = responseCode,
                failureMessage = "Purchase failed. Result code: $resultCode."
            )
            purchaseFinishedListener.onPurchasesUpdated(ResponseCode.ERROR.value, emptyList())
        }
    }

    private fun getResponseCodeFromIntent(intent: Intent): Int =
        intent.getIntExtra(RESPONSE_CODE, ResponseCode.ERROR.value)

    private fun getObjectFromJson(data: JSONObject, objectId: String): String =
        data.optString(objectId)

    private fun getResponseDesc(code: Int): String {
        val iabMsgs = (
            "0:OK/1:User Canceled/2:Unknown/" +
                "3:Billing Unavailable/4:Item unavailable/" +
                "5:Developer Error/6:Error/7:Item Already Owned/" +
                "8:Item not owned"
            ).split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val iabHelperMsgs = (
            "0:OK/-1001:Remote exception during initialization/" +
                "-1002:Bad response received/" +
                "-1003:Purchase signature verification failed/" +
                "-1004:Send intent failed/" +
                "-1005:User cancelled/" +
                "-1006:Unknown purchase response/" +
                "-1007:Missing token/" +
                "-1008:Unknown error/" +
                "-1009:Subscriptions not available/" +
                "-1010:Invalid consumption attempt"
            ).split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        return if (code <= minimumErrorLevel) {
            val index = minimumErrorLevel - code
            if (index < iabHelperMsgs.size) {
                iabHelperMsgs[index]
            } else {
                "$code:Unknown IAB Helper Error"
            }
        } else if (code < startingErrorCode || code >= iabMsgs.size) {
            "$code:Unknown"
        } else {
            iabMsgs[code]
        }
    }

    private const val startingErrorCode = 0
    private const val minimumErrorLevel = -1000
}
