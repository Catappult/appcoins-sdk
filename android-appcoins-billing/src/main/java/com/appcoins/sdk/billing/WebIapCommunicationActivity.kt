package com.appcoins.sdk.billing

import android.app.Activity
import android.os.Bundle
import com.appcoins.sdk.billing.usecases.HandlePurchaseResultFromWalletDeeplink
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import org.json.JSONObject
import java.net.URLDecoder

class WebIapCommunicationActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logInfo("Deeplink to SDK requested.")
        verifyPurchaseResult()
        finish()
    }

    private fun verifyPurchaseResult() {
        intent.data?.let { uri ->
            try {
                uri.getQueryParameter("purchaseResult")?.let { jsonString ->
                    val decodedJson = URLDecoder.decode(jsonString, "UTF-8")
                    val jsonObject = JSONObject(decodedJson)

                    val responseCode = jsonObject.getInt("responseCode")
                    val purchaseToken =
                        jsonObject.optString("purchaseToken").takeIf { it.isNotEmpty() }

                    HandlePurchaseResultFromWalletDeeplink(responseCode, purchaseToken)
                }
            } catch (e: Exception) {
                logError("There was an error with the Purchase Result from Deeplink.", e)
            }
        }
    }
}