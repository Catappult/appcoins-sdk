package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.managers.WalletManager.requestWallet
import com.appcoins.sdk.billing.mappers.VoidedPurchaseResponse
import com.appcoins.sdk.billing.mappers.VoidedPurchasesResponse
import com.appcoins.sdk.billing.repositories.VoidedPurchasesRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_3_SECS
import com.appcoins.sdk.core.logger.Logger.logInfo

object VoidedPurchasesManager {
    private val voidedPurchasesRepository =
        VoidedPurchasesRepository(BdsService(BuildConfig.HOST_WS, TIMEOUT_3_SECS))

    fun getVoidedPurchasesSync(
        packageName: String,
        walletId: String?,
        startTime: String?
    ): VoidedPurchasesResponse {
        logInfo("Getting Voided Purchases.")
        val walletGenerationModel = requestWallet(walletId)
        return voidedPurchasesRepository.getVoidedPurchasesSync(
            packageName,
            walletGenerationModel.ewt,
            startTime
        )
    }

    fun getVoidedPurchaseSync(
        packageName: String,
        walletId: String?,
        purchaseToken: String
    ): VoidedPurchaseResponse? {
        logInfo("Getting Voided Purchase.")
        val walletGenerationModel = requestWallet(walletId)
        return voidedPurchasesRepository.getVoidedPurchaseSync(
            packageName,
            walletGenerationModel.ewt,
            purchaseToken
        )
    }
}
