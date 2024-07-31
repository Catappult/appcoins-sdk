package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class AttributionResponseMapper {
    fun map(response: RequestResponse): AttributionResponse {
        WalletUtils.getSdkAnalytics()
            .sendCallBackendAttributionEvent(response.responseCode, response.response)

        if (!isSuccess(response.responseCode) || response.response == null) {
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
            it.printStackTrace()
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
