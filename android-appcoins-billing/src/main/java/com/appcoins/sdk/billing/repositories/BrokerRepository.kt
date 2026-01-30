package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.mappers.TransactionResponse
import com.appcoins.sdk.billing.mappers.TransactionResponseMapper
import com.appcoins.sdk.billing.mappers.TransactionsResponse
import com.appcoins.sdk.billing.mappers.TransactionsResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.date.parseIsoToMillis
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class BrokerRepository(private val bdsService: BdsService) {

    fun getTransaction(orderId: String): TransactionResponse? {
        val countDownLatch = CountDownLatch(1)
        var transaction: TransactionResponse? = null

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                requestResponse?.let {
                    val transactionResponse = TransactionResponseMapper().map(requestResponse)
                    transactionResponse.responseCode?.let { responseCode ->
                        if (ServiceUtils.isSuccess(responseCode)) {
                            transaction = transactionResponse
                        }
                    }
                }
                countDownLatch.countDown()
            }

        bdsService.makeRequest(
            "/broker/8.20240901/transactions/$orderId",
            "GET",
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap(),
            serviceResponseListener,
            SdkBackendRequestType.TRANSACTION
        )

        waitForCountDown(countDownLatch)
        return transaction
    }

    fun getIapTransactionsFromTimestamp(
        timestamp: Long,
        walletAddress: String,
        url: String? = null
    ): TransactionsResponse? {
        val countDownLatch = CountDownLatch(1)
        var transactions: TransactionsResponse? = null

        val serviceResponseListener = ServiceResponseListener { requestResponse ->
            transactions = processTransactionResponse(requestResponse, timestamp, walletAddress)
            countDownLatch.countDown()
        }

        val defaultEndpoint = "/broker/8.20240901/transactions"
        val endpoint = url?.removePrefix("") ?: defaultEndpoint
        val queryParams =
            if (endpoint == defaultEndpoint) {
                mapOf(
                    "wallet_from" to walletAddress,
                    "type" to "INAPP"
                )
            } else {
                emptyMap()
            }

        bdsService.makeRequest(
            endpoint,
            "GET",
            emptyList(),
            queryParams,
            emptyMap(),
            emptyMap(),
            serviceResponseListener,
            SdkBackendRequestType.TRANSACTION
        )

        waitForCountDown(countDownLatch)
        return transactions
    }

    private fun processTransactionResponse(
        requestResponse: RequestResponse,
        timestamp: Long,
        walletAddress: String
    ): TransactionsResponse? {
        val response = TransactionsResponseMapper().map(requestResponse)
        val responseCode = response.responseCode ?: return null

        if (!ServiceUtils.isSuccess(responseCode)) return null

        val allFetched = response.transactions
        val filtered = allFetched.filter { parseIsoToMillis(it.added) > timestamp }

        val shouldGetNext = filtered.size == allFetched.size && !response.nextUrl.isNullOrEmpty()

        return if (shouldGetNext) {
            val nextResponse = getIapTransactionsFromTimestamp(timestamp, walletAddress, response.nextUrl)

            val combinedTransactions = (filtered + (nextResponse?.transactions ?: emptyList())).toMutableList()
            response.copy(transactions = combinedTransactions)
        } else {
            response.copy(transactions = filtered.toMutableList())
        }
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout on BrokerRepository: $e")
        }
    }
}
