package com.appcoins.sdk.ingameupdates.mappers

import com.appcoins.sdk.ingameupdates.services.RequestResponse
import com.appcoins.sdk.ingameupdates.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class StoreLinkResponseMapper {
    fun map(response: RequestResponse): StoreLinkResponse {
        /*WalletUtils.getSdkAnalytics()
            .sendCallBackendWebPaymentUrlEvent(response.responseCode, response.response)*/

        if (!isSuccess(response.responseCode) || response.response == null) {
            return StoreLinkResponse(response.responseCode)
        }

        val deeplink = runCatching {
            JSONObject(response.response).optString("deeplink")
        }.getOrElse {
            it.printStackTrace()
            null
        }
        return StoreLinkResponse(response.responseCode, deeplink)
    }
}

data class StoreLinkResponse(
    val responseCode: Int?,
    val deeplink: String? = null
)
