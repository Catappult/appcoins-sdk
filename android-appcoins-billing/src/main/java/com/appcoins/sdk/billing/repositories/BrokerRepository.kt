package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.mappers.TransactionResponse
import com.appcoins.sdk.billing.mappers.TransactionResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
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

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout on BrokerRepository: $e")
        }
    }
}
