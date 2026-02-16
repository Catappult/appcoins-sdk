package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.WalletDetailsHelper
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.models.WalletDetails
import com.appcoins.sdk.billing.repositories.WalletRepository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AptoideWalletSharedPreferences
import com.appcoins.sdk.core.logger.Logger.logInfo

object WalletManager {
    private val walletRepository =
        WalletRepository(BdsService(BuildConfig.BACKEND_BASE, BdsService.TIME_OUT_IN_MILLIS))
    private val aptoideWalletSharedPreferences by lazy { AptoideWalletSharedPreferences(WalletUtils.context) }

    fun requestWallet(walletId: String?): WalletDetails {
        logInfo("Requesting Wallet value.")
        val storedWalletDetails = aptoideWalletSharedPreferences.getWalletDetails()
        if (!storedWalletDetails.hasError()) {
            if (WalletDetailsHelper().isWalletExpirationTimeValid(storedWalletDetails.expirationTimeMillis)) {
                logInfo("Stored Wallet is valid.")
                return storedWalletDetails
            }
        }
        val walletDetails = walletId?.let {
            walletRepository.requestWalletSync(it)
        } ?: WalletDetails.createErrorWalletDetails()

        aptoideWalletSharedPreferences.setWalletDetails(walletDetails)

        return walletDetails
    }
}
