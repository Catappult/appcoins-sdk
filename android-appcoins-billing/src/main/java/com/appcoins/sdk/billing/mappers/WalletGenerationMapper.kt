package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class WalletGenerationMapper {
    fun map(requestResponse: RequestResponse): WalletGenerationResponse {

        if (!isSuccess(requestResponse.responseCode) || requestResponse.response == null) {
            return WalletGenerationResponse()
        }

        runCatching {
            val response = requestResponse.response
            val jsonObject = JSONObject(response)
            val walletAddress = jsonObject.getString("address")
            val signature = jsonObject.getString("signature")

            return WalletGenerationResponse(walletAddress, signature, false)
        }.getOrElse {
            it.printStackTrace()
            return WalletGenerationResponse()
        }
    }
}

data class WalletGenerationResponse(
    val address: String,
    val signature: String,
    val error: Boolean
) {

    constructor() : this("", "", true)

    fun hasError(): Boolean {
        return error
    }
}
