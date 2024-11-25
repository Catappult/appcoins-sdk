package com.appcoins.sdk.billing

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import com.appcoins.sdk.billing.listeners.ExternalPaymentResponseStream
import com.appcoins.sdk.billing.models.ResponseType
import com.appcoins.sdk.billing.usecases.HandlePurchaseResultFromWalletDeeplink
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class WebIapCommunicationActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logInfo("Deeplink to SDK requested.")
        handleResult()
        finish()
    }

    private fun handleResult() {
        intent.data?.let { uri ->
            logDebug("$uri")
            val responseTypeReceived = getResponseTypeFromURI(uri)
            when (ResponseType.fromValue(responseTypeReceived)) {
                ResponseType.EXTERNAL_PAYMENT ->
                    ExternalPaymentResponseStream.getInstance().emit()

                else -> handleOldResult(uri)
            }
        }
    }

    private fun handleOldResult(uri: Uri) {
        val purchaseResult = uri.getQueryParameter(PURCHASE_RESULT_KEY)
        if (purchaseResult != null) {
            verifyPurchaseResult(purchaseResult)
        }
    }

    private fun verifyPurchaseResult(purchaseResult: String) {
        try {
            val decodedJson = URLDecoder.decode(purchaseResult, StandardCharsets.UTF_8.name())
            val jsonObject = JSONObject(decodedJson)

            val responseCode = jsonObject.getInt(RESPONSE_CODE_KEY)
            val purchaseToken =
                jsonObject.optString(PURCHASE_TOKEN_KEY).takeIf { it.isNotEmpty() }
            logInfo("Received Purchase Result from Wallet Deeplink. ResponseCode: $responseCode")
            HandlePurchaseResultFromWalletDeeplink(responseCode, purchaseToken)
        } catch (e: Exception) {
            logError("There was an error with the Purchase Result from Deeplink.", e)
            HandlePurchaseResultFromWalletDeeplink(ResponseCode.ERROR.value, null)
        }
    }

    private fun getResponseTypeFromURI(uri: Uri): Int? {
        var responseType: Int? = null
        uri.getQueryParameter(RESPONSE_TYPE_KEY)?.let { responseTypeReceived ->
            try {
                responseType = responseTypeReceived.toInt()
            } catch (ex: Exception) {
                logError("Failed to parse ResponseType.", ex)
            }
        }

        return responseType
    }

    private companion object {
        const val RESPONSE_TYPE_KEY = "responseType"

        const val PURCHASE_RESULT_KEY = "purchaseResult"
        const val PURCHASE_TOKEN_KEY = "purchaseToken"
        const val RESPONSE_CODE_KEY = "responseCode"
    }
}
