package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class AttributionResponseMapper {
    fun map(response: RequestResponse): AttributionResponse {
        WalletUtils.sdkAnalytics.sendCallBackendAttributionEvent(
            response.responseCode,
            response.response,
            response.exception?.toString()
        )

        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Attribution Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return AttributionResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)
            val packageName =
                responseJSONObject.optString("package_name").takeIf { it.isNotEmpty() }
            val oemId = responseJSONObject.optString("oemid").takeIf { it.isNotEmpty() }
            val guestId = responseJSONObject.optString("guest_uid").takeIf { it.isNotEmpty() }
            val utmSource = responseJSONObject.optString("utm_source").takeIf { it.isNotEmpty() }
            val utmMedium = responseJSONObject.optString("utm_medium").takeIf { it.isNotEmpty() }
            val utmCampaign =
                responseJSONObject.optString("utm_campaign").takeIf { it.isNotEmpty() }
            val utmTerm = responseJSONObject.optString("utm_term").takeIf { it.isNotEmpty() }
            val utmContent = responseJSONObject.optString("utm_content").takeIf { it.isNotEmpty() }
            return AttributionResponse(
                response.responseCode,
                packageName,
                oemId,
                guestId,
                utmSource,
                utmMedium,
                utmCampaign,
                utmTerm,
                utmContent
            )
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            return AttributionResponse(response.responseCode)
        }
    }
}

data class AttributionResponse(
    val responseCode: Int?,
    val packageName: String? = null,
    val oemId: String? = null,
    val walletId: String? = null,
    val utmSource: String? = null,
    val utmMedium: String? = null,
    val utmCampaign: String? = null,
    val utmTerm: String? = null,
    val utmContent: String? = null,
)
