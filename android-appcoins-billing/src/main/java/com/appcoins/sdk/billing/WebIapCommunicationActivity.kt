package com.appcoins.sdk.billing

import android.app.Activity
import android.os.Bundle
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
        verifyPurchaseResult()
        finish()
    }

    private fun verifyPurchaseResult() {
        intent.data?.let { uri ->
            logDebug("$uri")
            uri.getQueryParameter(PURCHASE_RESULT_KEY)?.let { jsonString ->
                try {
                    val decodedJson = URLDecoder.decode(jsonString, StandardCharsets.UTF_8.name())
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
        }
    }

    private companion object {
        const val PURCHASE_RESULT_KEY = "purchaseResult"
        const val PURCHASE_TOKEN_KEY = "purchaseToken"
        const val RESPONSE_CODE_KEY = "responseCode"
    }
}