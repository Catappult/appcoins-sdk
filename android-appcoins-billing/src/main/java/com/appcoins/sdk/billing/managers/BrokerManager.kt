package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.managers.WalletManager.requestWallet
import com.appcoins.sdk.billing.mappers.TransactionResponse
import com.appcoins.sdk.billing.repositories.BrokerRepository
import com.appcoins.sdk.billing.service.BdsService

object BrokerManager {
    private val brokerRepository = BrokerRepository(BdsService(BuildConfig.HOST_WS, 3000))

    fun getTransaction(orderId: String): TransactionResponse? =
        brokerRepository.getTransaction(orderId)

    fun consumePurchase(walletId: String, packageName: String, purchaseToken: String): Int {
        val walletGenerationModel = requestWallet(walletId)
        return brokerRepository.consumePurchaseSync(
            walletGenerationModel.walletAddress,
            walletGenerationModel.signature,
            packageName,
            purchaseToken
        )
    }
}
