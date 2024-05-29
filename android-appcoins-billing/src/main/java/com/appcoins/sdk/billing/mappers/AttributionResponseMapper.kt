package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class AttributionResponseMapper {
    fun map(response: RequestResponse): AttributionResponse {
        /*WalletUtils.getSdkAnalytics()
            .sendCallBackendPayflowEvent(response.responseCode, response.response)*/

        if (!isSuccess(response.responseCode) || response.response == null) {
            return AttributionResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)
            val oemId = responseJSONObject.optString("oemId")
            val guestId = responseJSONObject.optString("guestId")
            return AttributionResponse(response.responseCode, oemId, guestId)
        }.getOrElse {
            it.printStackTrace()
            return AttributionResponse(response.responseCode)
        }
    }
}

data class AttributionResponse(
    val responseCode: Int?,
    val oemId: String? = null,
    val walletId: String? = null
)
