package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class TransactionResponseMapper {
    fun map(response: RequestResponse): TransactionResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Transaction. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return TransactionResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)
            val uid = responseJSONObject.optString("uid").takeIf { it.isNotEmpty() }
            val domain = responseJSONObject.optString("domain").takeIf { it.isNotEmpty() }
            val product = responseJSONObject.optString("product").takeIf { it.isNotEmpty() }
            val walletFrom = responseJSONObject.optString("wallet_from").takeIf { it.isNotEmpty() }
            val type = responseJSONObject.optString("type").takeIf { it.isNotEmpty() }
            val method = responseJSONObject.optString("method").takeIf { it.isNotEmpty() }
            val country = responseJSONObject.optString("country").takeIf { it.isNotEmpty() }
            val reference = responseJSONObject.optString("reference").takeIf { it.isNotEmpty() }
            val hash = responseJSONObject.optString("hash").takeIf { it.isNotEmpty() }
            val status = responseJSONObject.optString("status").takeIf { it.isNotEmpty() }
            val added = responseJSONObject.optString("added").takeIf { it.isNotEmpty() }
            val modified = responseJSONObject.optString("modified").takeIf { it.isNotEmpty() }

            val gatewayJson = responseJSONObject.optJSONObject("gateway")
            val gateway = gatewayJson?.let { gateway ->
                Gateway(
                    name = gateway.optString("name").takeIf { it.isNotEmpty() }
                )
            }

            val metadataJson = responseJSONObject.optJSONObject("metadata")
            val metadata = metadataJson?.let { metadata ->
                Metadata(
                    renewal = metadata.optBoolean("renewal"),
                    purchaseUid = metadata.optString("purchase_uid").takeIf { it.isNotEmpty() }
                )
            }

            val priceJson = responseJSONObject.optJSONObject("price")
            val price = priceJson?.let { price ->
                Price(
                    currency = price.optString("currency").takeIf { it.isNotEmpty() },
                    value = price.optString("value").takeIf { it.isNotEmpty() },
                    appc = price.optString("appc").takeIf { it.isNotEmpty() },
                    usd = price.optString("usd").takeIf { it.isNotEmpty() },
                    vat = price.optString("vat").takeIf { it.isNotEmpty() },
                    discount = price.optString("discount").takeIf { it.isNotEmpty() }
                )
            }

            val channel = responseJSONObject.optString("channel").takeIf { it.isNotEmpty() }

            return TransactionResponse(
                responseCode = response.responseCode,
                uid = uid,
                domain = domain,
                product = product,
                walletFrom = walletFrom,
                type = type,
                method = method,
                country = country,
                reference = reference,
                hash = hash,
                status = status,
                added = added,
                modified = modified,
                gateway = gateway,
                metadata = metadata,
                price = price,
                channel = channel
            )
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.TRANSACTION,
                response.response,
                Exception(it).toString()
            )
            return TransactionResponse(response.responseCode)
        }
    }
}

data class Gateway(
    val name: String? = null
)

data class Metadata(
    val renewal: Boolean? = null,
    val purchaseUid: String? = null
)

data class Price(
    val currency: String? = null,
    val value: String? = null,
    val appc: String? = null,
    val usd: String? = null,
    val vat: String? = null,
    val discount: String? = null
)

data class TransactionResponse(
    val responseCode: Int?,
    val uid: String? = null,
    val domain: String? = null,
    val product: String? = null,
    val walletFrom: String? = null,
    val type: String? = null,
    val method: String? = null,
    val country: String? = null,
    val reference: String? = null,
    val hash: String? = null,
    val status: String? = null,
    val added: String? = null,
    val modified: String? = null,
    val gateway: Gateway? = null,
    val metadata: Metadata? = null,
    val price: Price? = null,
    val channel: String? = null
)
