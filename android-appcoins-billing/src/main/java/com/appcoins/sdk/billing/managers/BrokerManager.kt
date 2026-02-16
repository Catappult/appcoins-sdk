package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.managers.WalletManager.requestWallet
import com.appcoins.sdk.billing.mappers.TransactionResponse
import com.appcoins.sdk.billing.mappers.TransactionsResponse
import com.appcoins.sdk.billing.repositories.BrokerRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_3_SECS
import com.appcoins.sdk.core.logger.Logger.logInfo

object BrokerManager {

    private val brokerRepository =
        BrokerRepository(BdsService(BuildConfig.HOST_WS, TIMEOUT_3_SECS))

    fun getTransaction(orderId: String): TransactionResponse? {
        logInfo("Getting transaction value.")
        return brokerRepository.getTransaction(orderId)
    }

    fun getIapTransactionsFromTimestamp(timestamp: Long): TransactionsResponse? {
        logInfo("Getting transactions from timestamp.")
        val walletId = AttributionSharedPreferences(WalletUtils.context).getWalletId()
        val walletDetails = requestWallet(walletId)
        if (walletDetails.hasError()) {
            return null
        }
        return brokerRepository.getIapTransactionsFromTimestamp(timestamp, walletDetails.walletAddress)
    }
}
