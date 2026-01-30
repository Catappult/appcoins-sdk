package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class TransactionsResponseMapper {
    fun map(response: RequestResponse): TransactionsResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Transactions. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return TransactionsResponse(response.responseCode)
        }

        runCatching {
            val responseJSONObject = JSONObject(response.response)

            val transactions = mutableListOf<Transaction>()

            val nextUrl: String? =
                responseJSONObject.optJSONObject("next")?.optString("url")?.takeIf { it.isNotEmpty() }

            responseJSONObject.optJSONArray("items")?.let { transactionsArray ->
                for (i in 0 until transactionsArray.length()) {
                    val transactionJSONObject = transactionsArray.getJSONObject(i)
                    val transaction = TransactionResponseMapper().mapTransactionObject(transactionJSONObject)
                    transactions.add(transaction)
                }
            }

            return TransactionsResponse(
                responseCode = response.responseCode,
                nextUrl = nextUrl,
                transactions = transactions
            )
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.TRANSACTION,
                response.response,
                Exception(it).toString()
            )
            return TransactionsResponse(response.responseCode)
        }
    }
}

data class TransactionsResponse(
    val responseCode: Int?,
    val nextUrl: String? = null,
    val transactions: MutableList<Transaction> = mutableListOf()
)
