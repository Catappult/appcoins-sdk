package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.helpers.WalletDetailsHelper
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class WalletDetailsMapper {
    fun map(requestResponse: RequestResponse): WalletDetailsResponse {
        if (!isSuccess(requestResponse.responseCode) || requestResponse.response == null) {
            logError(
                "Failed to obtain Wallet Values. " +
                    "ResponseCode: ${requestResponse.responseCode} | Cause: ${requestResponse.exception}"
            )
            return WalletDetailsResponse()
        }

        runCatching {
            val response = requestResponse.response
            val jsonObject = JSONObject(response)
            val walletAddress = jsonObject.getString("address")
            val walletToken = jsonObject.getString("wallet_token")
            val expirationTimeMillis =
                jsonObject.optLong("expires_at")
                    .takeIf { it != 0L }
                    ?: WalletDetailsHelper().extractExpirationTimeMillisFromWalletToken(walletToken)
                    ?: 0L

            return WalletDetailsResponse(walletAddress, walletToken, expirationTimeMillis, false)
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.GUEST_WALLET,
                requestResponse.response,
                Exception(it).toString()
            )
            return WalletDetailsResponse()
        }
    }
}

data class WalletDetailsResponse(
    val walletAddress: String,
    val walletToken: String,
    val expirationTimeMillis: Long,
    val error: Boolean,
) {

    constructor() : this("", "", 0L, true)

    fun hasError(): Boolean {
        return error
    }
}
