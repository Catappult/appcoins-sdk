package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class PurchaseResponseMapper {
    fun map(response: RequestResponse): PurchaseResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Purchase Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return PurchaseResponse(response.responseCode)
        }

        runCatching {
            try {
                val itemJson = JSONObject(response.response)

                val uid = itemJson.optString("uid")
                val sku = itemJson.optString("sku")
                val state = itemJson.optString("state")
                val orderUid =
                    itemJson.optString("order_uid")
                val payload = itemJson.optString("payload").takeIf { it.isNotEmpty() }
                val created = itemJson.optString("created")

                val verificationJson = itemJson.getJSONObject("verification")
                val verification =
                    Verification(
                        type = verificationJson.optString("type"),
                        data = verificationJson.optString("data"),
                        signature = verificationJson.optString("signature"),
                    )

                return PurchaseResponse(
                    responseCode = response.responseCode,
                    purchase = Purchase(
                        uid = uid,
                        sku = sku,
                        state = state,
                        orderUid = orderUid,
                        payload = payload,
                        created = created,
                        verification = verification
                    )
                )
            } catch (e: Exception) {
                SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                    SdkBackendRequestType.PURCHASE,
                    response.response,
                    e.toString()
                )
                logError("There was an error mapping the Purchase response: $e")
            }

            return PurchaseResponse(response.responseCode)
        }.getOrElse {
            logError("There was an error mapping the List of Purchases response: " + Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.PURCHASE,
                response.response,
                Exception(it).toString()
            )
            return PurchaseResponse(response.responseCode)
        }
    }
}

data class PurchaseResponse(
    val responseCode: Int,
    val purchase: Purchase? = null
)
