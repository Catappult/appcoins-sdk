package com.appcoins.sdk.billing.helpers

import android.util.Base64
import com.appcoins.sdk.core.date.SECONDS_TO_MILLIS
import org.json.JSONObject

class WalletDetailsHelper {
    fun extractExpirationTimeMillisFromWalletToken(walletToken: String): Long? {
        return try {
            val parts = walletToken.split(".")
            if (parts.size < 2) return null

            val payloadJson = String(Base64.decode(parts[1], Base64.DEFAULT), Charsets.UTF_8)

            val payload = JSONObject(payloadJson)

            val expirationTimeMillis = payload.optLong("exp").takeIf { it != 0L }
            expirationTimeMillis ?: return null
        } catch (_: Exception) {
            null
        }
    }

    fun isWalletExpirationTimeValid(expirationTimeSeconds: Long): Boolean {
        val nowMillis = System.currentTimeMillis()
        val nowSeconds = nowMillis / SECONDS_TO_MILLIS

        return nowSeconds < expirationTimeSeconds
    }
}
