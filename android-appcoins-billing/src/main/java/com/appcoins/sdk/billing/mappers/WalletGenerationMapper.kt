package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class WalletGenerationMapper {
    fun map(requestResponse: RequestResponse): WalletGenerationResponse {
        if (!isSuccess(requestResponse.responseCode) || requestResponse.response == null) {
            logError(
                "Failed to obtain Wallet Values. " +
                    "ResponseCode: ${requestResponse.responseCode} | Cause: ${requestResponse.exception}"
            )
            return WalletGenerationResponse()
        }

        runCatching {
            val response = requestResponse.response
            val jsonObject = JSONObject(response)
            val walletAddress = jsonObject.getString("address")
            val signature = jsonObject.getString("signature")
            val ewt = jsonObject.getString("ewt")

            return WalletGenerationResponse(walletAddress, signature, ewt, false)
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.GUEST_WALLET,
                requestResponse.response,
                Exception(it).toString()
            )
            return WalletGenerationResponse()
        }
    }
}

data class WalletGenerationResponse(
    val address: String,
    val signature: String,
    val ewt: String,
    val error: Boolean
) {

    constructor() : this("", "", "", true)

    fun hasError(): Boolean {
        return error
    }
}
