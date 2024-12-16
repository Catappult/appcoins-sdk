package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class ReferralDeeplinkResponseMapper {
    fun map(response: RequestResponse): ReferralDeeplinkResponse {
        WalletUtils.sdkAnalytics.sendCallBackendReferralDeeplinkEvent(
            response.responseCode,
            response.response,
            response.exception?.toString()
        )

        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Referral Deeplink Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return ReferralDeeplinkResponse(response.responseCode)
        }

        return try {
            val responseJsonObject = JSONObject(response.response)

            val storeDeeplink = responseJsonObject.optString("store_deeplink").takeIf { it.isNotEmpty() }
            val fallbackDeeplink = responseJsonObject.optString("fallback_deeplink").takeIf { it.isNotEmpty() }

            ReferralDeeplinkResponse(response.responseCode, storeDeeplink, fallbackDeeplink)
        } catch (ex: Exception) {
            logError("There was a an error mapping the response.", ex)
            ReferralDeeplinkResponse(response.responseCode)
        }
    }
}

data class ReferralDeeplinkResponse(
    val responseCode: Int?,
    val storeDeeplink: String? = null,
    val fallbackDeeplink: String? = null
)
