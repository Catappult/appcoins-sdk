package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class StoreLinkResponseMapper {
    fun map(response: RequestResponse): StoreLinkResponse {
        WalletUtils.sdkAnalytics.sendCallBackendStoreLinkEvent(
            response.responseCode,
            response.response,
            response.exception?.toString()
        )

        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain StoreLink Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return StoreLinkResponse(response.responseCode)
        }

        val deeplink = runCatching {
            JSONObject(response.response).optString("deeplink").takeIf { it.isNotEmpty() }
        }.getOrElse {
            logError("There was a an error mapping the response.", Exception(it))
            null
        }
        return StoreLinkResponse(response.responseCode, deeplink)
    }
}

data class StoreLinkResponse(
    val responseCode: Int?,
    val deeplink: String? = null
)
