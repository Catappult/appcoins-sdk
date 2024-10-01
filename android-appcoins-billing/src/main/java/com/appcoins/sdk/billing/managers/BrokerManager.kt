package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.mappers.TransactionResponse
import com.appcoins.sdk.billing.repositories.BrokerRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.core.logger.Logger.logInfo

object BrokerManager {
    private const val TIMEOUT_IN_MILLIS = 3000

    private val brokerRepository =
        BrokerRepository(BdsService(BuildConfig.HOST_WS, TIMEOUT_IN_MILLIS))

    fun getTransaction(orderId: String): TransactionResponse? {
        logInfo("Getting transaction value.")
        return brokerRepository.getTransaction(orderId)
    }
}
