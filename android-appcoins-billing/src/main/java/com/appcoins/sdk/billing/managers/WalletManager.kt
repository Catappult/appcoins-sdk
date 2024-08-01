package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.analytics.WalletAddressProvider
import com.appcoins.sdk.billing.models.WalletGenerationModel
import com.appcoins.sdk.billing.repositories.WalletRepository
import com.appcoins.sdk.billing.service.BdsService

object WalletManager {
    private val walletRepository =
        WalletRepository(
            BdsService(BuildConfig.BACKEND_BASE, BdsService.TIME_OUT_IN_MILLIS),
            WalletAddressProvider.provideWalletAddressProvider()
        )

    fun requestWallet(walletId: String): WalletGenerationModel {
        val walletGenerationModel = walletRepository.requestWalletSync(walletId)

        return walletGenerationModel
    }
}