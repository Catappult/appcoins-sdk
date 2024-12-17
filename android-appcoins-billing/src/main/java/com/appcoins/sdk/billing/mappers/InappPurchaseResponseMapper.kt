package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class InappPurchaseResponseMapper {
    fun map(response: RequestResponse): InappPurchaseResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Purchase Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return InappPurchaseResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)
            val uid = responseJSONObject.optString("uid").takeIf { it.isNotEmpty() }
            val sku = responseJSONObject.optString("sku").takeIf { it.isNotEmpty() }
            val domain = responseJSONObject.optString("domain").takeIf { it.isNotEmpty() }
            val type = responseJSONObject.optString("type").takeIf { it.isNotEmpty() }
            val status = responseJSONObject.optString("status").takeIf { it.isNotEmpty() }
            val state = responseJSONObject.optString("state").takeIf { it.isNotEmpty() }
            val payload = responseJSONObject.optString("payload").takeIf { it.isNotEmpty() }
            val created = responseJSONObject.optString("created").takeIf { it.isNotEmpty() }

            val buyerJson = responseJSONObject.optJSONObject("buyer")
            val buyer = buyerJson?.let { buyer ->
                Buyer(
                    type = buyer.optString("type").takeIf { it.isNotEmpty() },
                    reference = buyer.optString("reference").takeIf { it.isNotEmpty() }
                )
            }

            val orderJson = responseJSONObject.optJSONObject("order")
            val order = orderJson?.let { order ->
                Order(
                    uid = order.optString("uid").takeIf { it.isNotEmpty() },
                    gateway = order.optString("gateway").takeIf { it.isNotEmpty() },
                    reference = order.optString("reference").takeIf { it.isNotEmpty() },
                    status = order.optString("status").takeIf { it.isNotEmpty() },
                    created = order.optString("created").takeIf { it.isNotEmpty() }
                )
            }

            return InappPurchaseResponse(
                responseCode = response.responseCode,
                uid = uid,
                sku = sku,
                domain = domain,
                type = type,
                status = status,
                state = state,
                payload = payload,
                created = created,
                buyer = buyer,
                order = order
            )
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            return InappPurchaseResponse(response.responseCode)
        }
    }
}

data class Buyer(
    val type: String? = null,
    val reference: String? = null
)

data class Order(
    val uid: String? = null,
    val gateway: String? = null,
    val reference: String? = null,
    val status: String? = null,
    val created: String? = null
)

data class InappPurchaseResponse(
    val responseCode: Int?,
    val uid: String? = null,
    val sku: String? = null,
    val domain: String? = null,
    val type: String? = null,
    val status: String? = null,
    val state: String? = null,
    val payload: String? = null,
    val created: String? = null,
    val buyer: Buyer? = null,
    val order: Order? = null
)
