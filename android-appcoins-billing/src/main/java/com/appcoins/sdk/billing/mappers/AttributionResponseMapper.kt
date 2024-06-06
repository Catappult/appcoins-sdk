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
            val packageName = responseJSONObject.optString("package_name")
            val oemId = responseJSONObject.optString("oemid")
            val guestId = responseJSONObject.optString("guest_uid")
            return AttributionResponse(response.responseCode, packageName, oemId, guestId)
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
    val walletId: String? = null
)
