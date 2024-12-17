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

        val storeLinkMethods = arrayListOf<StoreLinkMethod>()
        try {
            val responseJsonObject = JSONObject(response.response)
            responseJsonObject.optJSONArray("store_link_methods")?.let { storeLinkMethodJsonArray ->
                for (i in 0 until storeLinkMethodJsonArray.length()) {
                    val storeLinkMethodJSONObject = storeLinkMethodJsonArray.optJSONObject(i)

                    val deeplink = storeLinkMethodJSONObject.optString("deeplink")
                    val priority = storeLinkMethodJSONObject.optInt("priority", -1)

                    storeLinkMethods.add(StoreLinkMethod(deeplink, priority))
                }
                storeLinkMethods.sortBy { it.priority }
            }
        } catch (ex: Exception) {
            logError("There was a an error mapping the response.", ex)
        }
        return StoreLinkResponse(
            response.responseCode,
            storeLinkMethods.filter { it.priority >= 0 }.toCollection(arrayListOf())
        )
    }
}

data class StoreLinkResponse(
    val responseCode: Int?,
    val storeLinkMethods: ArrayList<StoreLinkMethod> = arrayListOf()
)

data class StoreLinkMethod(
    val deeplink: String,
    val priority: Int,
)
