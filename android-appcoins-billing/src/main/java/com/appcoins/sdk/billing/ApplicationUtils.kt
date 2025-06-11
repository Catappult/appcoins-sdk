package com.appcoins.sdk.billing

import android.app.Activity
import android.content.Intent
import android.util.Base64
import com.appcoins.sdk.billing.CatapultAppcoinsBilling.BillingResponseCode
import com.appcoins.sdk.billing.helpers.BillingResultHelper
import com.appcoins.sdk.billing.usecases.mmp.SendSuccessfulPurchaseResponseEvent
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.appcoins.sdk.core.analytics.SdkAnalytics
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.security.PurchasesSecurityHelper
import org.json.JSONObject

internal object ApplicationUtils {

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    @JvmStatic
    fun handleActivityResult(resultCode: Int, data: Intent?, purchaseFinishedListener: PurchasesUpdatedListener) {
        val sdkAnalytics = SdkAnalyticsUtils.sdkAnalytics

        if (data == null) {
            handleDataNull(purchaseFinishedListener, sdkAnalytics)
            return
        }

        val responseCode = getResponseCodeFromIntent(data)
        val purchaseData = data.getStringExtra(INAPP_PURCHASE_DATA)
        val dataSignature = data.getStringExtra(INAPP_DATA_SIGNATURE)

        if (resultCode == Activity.RESULT_OK && responseCode == BillingResponseCode.OK) {
            handleSuccessfulResult(
                responseCode,
                data,
                purchaseData,
                dataSignature,
                purchaseFinishedListener,
                sdkAnalytics
            )
        } else if (resultCode == Activity.RESULT_OK) {
            // result code was OK, but in-app billing response was not OK.
            handleFailureBillingResult(responseCode, data, purchaseFinishedListener, sdkAnalytics)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            handleCanceledResult(responseCode, data, purchaseFinishedListener, sdkAnalytics)
        } else {
            handleUnknownFailureResult(resultCode, responseCode, data, purchaseFinishedListener, sdkAnalytics)
        }
    }

    private fun handleSuccessfulResult(
        responseCode: Int,
        data: Intent,
        purchaseData: String?,
        dataSignature: String?,
        purchaseFinishedListener: PurchasesUpdatedListener,
        sdkAnalytics: SdkAnalytics
    ) {
        logInfo("Successful ResultCode from Purchase.")
        logDebug("Purchase data: $purchaseData")
        logDebug("Data signature: $dataSignature")
        logDebug("Extras: " + data.extras)

        if (purchaseData == null || dataSignature == null) {
            handlePurchaseDataNull(purchaseFinishedListener, sdkAnalytics, data)
            return
        }

        if (PurchasesSecurityHelper.verifyPurchase(purchaseData, Base64.decode(dataSignature, Base64.DEFAULT))) {
            try {
                val purchaseDataJSON = JSONObject(purchaseData)
                val accountIdentifiers =
                    getObjectFromJson(purchaseDataJSON, "obfuscatedExternalAccountId")?.let { AccountIdentifiers(it) }
                val purchase =
                    Purchase(
                        accountIdentifiers,
                        getObjectFromJson(purchaseDataJSON, "developerPayload"),
                        getObjectFromJson(purchaseDataJSON, "orderId", ""),
                        purchaseData,
                        getObjectFromJson(purchaseDataJSON, "packageName", ""),
                        listOf(getObjectFromJson(purchaseDataJSON, "productId", "")),
                        Integer.decode(getObjectFromJson(purchaseDataJSON, "purchaseState", "0")),
                        getObjectFromJson(purchaseDataJSON, "purchaseTime", "0").toLong(),
                        getObjectFromJson(purchaseDataJSON, "purchaseToken", ""),
                        dataSignature,
                        getObjectFromJson(purchaseDataJSON, "isAutoRenewing", "false").toBoolean()
                    )

                val purchases: MutableList<Purchase> = ArrayList()
                purchases.add(purchase)
                SendSuccessfulPurchaseResponseEvent.invoke(purchase)
                sdkAnalytics.sendPurchaseResultEvent(
                    responseCode,
                    purchase.purchaseToken,
                    purchase.products.first()
                )
                purchaseFinishedListener.onPurchasesUpdated(
                    BillingResult.newBuilder().setResponseCode(responseCode).build(),
                    purchases
                )
                logInfo("Purchase result successfully sent.")
            } catch (e: Exception) {
                logError("Failed to parse purchase data: $e")
                sdkAnalytics.sendPurchaseResultEvent(
                    responseCode = BillingResponseCode.ERROR,
                    failureMessage = "Purchase failed with parsing error."
                )
                purchaseFinishedListener
                    .onPurchasesUpdated(
                        BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR).build(),
                        emptyList()
                    )
            }
        } else {
            handleSignatureVerificationFailed(purchaseFinishedListener, sdkAnalytics)
        }
    }

    private fun handleDataNull(purchaseFinishedListener: PurchasesUpdatedListener, sdkAnalytics: SdkAnalytics) {
        logError("Null data in IAB activity result.")
        sdkAnalytics.sendPurchaseResultEvent(
            responseCode = BillingResponseCode.ERROR,
            failureMessage = "Null data in IAB activity result."
        )
        purchaseFinishedListener.onPurchasesUpdated(
            BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR).build(),
            emptyList()
        )
    }

    private fun handlePurchaseDataNull(
        purchaseFinishedListener: PurchasesUpdatedListener,
        sdkAnalytics: SdkAnalytics,
        data: Intent
    ) {
        logError("BUG: either purchaseData or dataSignature is null.")
        logDebug("Extras: " + data.extras)
        sdkAnalytics.sendPurchaseResultEvent(
            responseCode = BillingResponseCode.ERROR,
            failureMessage = "Either purchaseData or dataSignature is null."
        )
        purchaseFinishedListener.onPurchasesUpdated(
            BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR).build(),
            emptyList()
        )
    }

    private fun handleSignatureVerificationFailed(
        purchaseFinishedListener: PurchasesUpdatedListener,
        sdkAnalytics: SdkAnalytics
    ) {
        logError("Signature verification failed.")
        sdkAnalytics.sendPurchaseResultEvent(
            responseCode = BillingResponseCode.DEVELOPER_ERROR,
            failureMessage = "Signature verification failed."
        )
        purchaseFinishedListener.onPurchasesUpdated(
            BillingResultHelper.buildBillingResult(
                BillingResponseCode.DEVELOPER_ERROR,
                BillingResultHelper.ERROR_TYPE_INVALID_PUBLIC_KEY
            ),
            emptyList()
        )
    }

    private fun handleFailureBillingResult(
        responseCode: Int,
        data: Intent,
        purchaseFinishedListener: PurchasesUpdatedListener,
        sdkAnalytics: SdkAnalytics
    ) {
        logError(
            "Result code was OK but in-app billing response was not OK: " +
                getResponseDesc(responseCode)
        )
        logDebug("Bundle: $data")
        sdkAnalytics.sendPurchaseResultEvent(
            responseCode = responseCode,
            failureMessage = "Result code was OK but in-app billing response was not OK."
        )
        purchaseFinishedListener.onPurchasesUpdated(
            BillingResult.newBuilder().setResponseCode(responseCode).build(),
            emptyList()
        )
    }

    private fun handleCanceledResult(
        responseCode: Int,
        data: Intent,
        purchaseFinishedListener: PurchasesUpdatedListener,
        sdkAnalytics: SdkAnalytics
    ) {
        logInfo("Purchase canceled - Response: " + getResponseDesc(responseCode))
        logDebug("Bundle: $data")
        sdkAnalytics.sendPurchaseResultEvent(responseCode = BillingResponseCode.USER_CANCELED)
        purchaseFinishedListener.onPurchasesUpdated(
            BillingResult.newBuilder().setResponseCode(BillingResponseCode.USER_CANCELED).build(),
            emptyList()
        )
    }

    private fun handleUnknownFailureResult(
        resultCode: Int,
        responseCode: Int,
        data: Intent,
        purchaseFinishedListener: PurchasesUpdatedListener,
        sdkAnalytics: SdkAnalytics
    ) {
        logError(
            "Purchase failed. Result code: $resultCode. Response: " +
                getResponseDesc(responseCode)
        )
        logDebug("Bundle: $data")
        sdkAnalytics.sendPurchaseResultEvent(
            responseCode = responseCode,
            failureMessage = "Purchase failed. Result code: $resultCode."
        )
        purchaseFinishedListener.onPurchasesUpdated(
            BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR).build(),
            emptyList()
        )
    }

    private fun getResponseCodeFromIntent(intent: Intent): Int =
        intent.getIntExtra(RESPONSE_CODE, BillingResponseCode.ERROR)

    private fun getObjectFromJson(data: JSONObject, objectId: String): String? =
        data.optString(objectId).takeIf { it.isNotEmpty() }

    private fun getObjectFromJson(data: JSONObject, objectId: String, defaultValue: String): String =
        data.optString(objectId).takeIf { it.isNotEmpty() } ?: defaultValue

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
