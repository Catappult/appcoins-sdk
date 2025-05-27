package com.appcoins.sdk.core.security

import android.util.Base64
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils.sdkAnalytics

object PurchasesSecurityHelper {

    lateinit var base64DecodedPublicKey: ByteArray

    fun verifyPurchase(purchaseData: String?, decodeSignature: ByteArray?): Boolean {
        val result = Security.verifyPurchase(base64DecodedPublicKey, purchaseData, decodeSignature)

        if (!result) {
            val keyEncoded = try {
                Base64.encodeToString(base64DecodedPublicKey, Base64.DEFAULT)
            } catch (exception: Exception) {
                null
            }
            sdkAnalytics
                .sendPurchaseSignatureVerificationFailureEvent(purchaseData ?: "", keyEncoded)
        }

        return result
    }
}
