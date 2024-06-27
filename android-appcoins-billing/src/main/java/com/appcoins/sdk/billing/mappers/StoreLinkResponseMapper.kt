package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class StoreLinkResponseMapper {
    fun map(response: RequestResponse): StoreLinkResponse {
        WalletUtils.getSdkAnalytics()
            .sendCallBackendStoreLinkEvent(response.responseCode, response.response)

        if (!isSuccess(response.responseCode) || response.response == null) {
            return StoreLinkResponse(response.responseCode)
        }

        val deeplink = runCatching {
            JSONObject(response.response).optString("deeplink").takeIf { it.isNotEmpty() }
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
