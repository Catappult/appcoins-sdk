package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.managers.WalletManager.requestWallet
import com.appcoins.sdk.billing.mappers.InappPurchaseResponse
import com.appcoins.sdk.billing.mappers.PurchaseResponse
import com.appcoins.sdk.billing.mappers.PurchasesResponse
import com.appcoins.sdk.billing.mappers.SkuDetailsResponse
import com.appcoins.sdk.billing.repositories.ProductV2Repository
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.TIMEOUT_3_SECS
import com.appcoins.sdk.core.logger.Logger.logInfo

object ProductV2Manager {
    private val productV2Repository =
        ProductV2Repository(BdsService(BuildConfig.HOST_WS, TIMEOUT_3_SECS))

    fun getInappPurchase(purchaseToken: String): InappPurchaseResponse? {
        logInfo("Getting InappPurchase value.")
        return productV2Repository.getInappPurchase(purchaseToken)
    }

    fun getPurchasesSync(
        packageName: String,
        walletId: String?,
        type: String
    ): PurchasesResponse {
        logInfo("Getting Purchases.")
        val walletGenerationModel = requestWallet(walletId)
        return productV2Repository.getPurchasesSync(
            packageName,
            walletGenerationModel.walletAddress,
            walletGenerationModel.signature,
            type
        )
    }

    fun getPurchaseSync(
        packageName: String,
        walletId: String?,
        purchaseToken: String
    ): PurchaseResponse? {
        logInfo("Getting Purchase.")
        val walletGenerationModel = requestWallet(walletId)
        return productV2Repository.getPurchaseSync(
            packageName,
            walletGenerationModel.ewt,
            purchaseToken
        )
    }

    fun consumePurchase(walletId: String, packageName: String, purchaseToken: String): Int {
        logInfo("Consuming purchase.")
        val walletGenerationModel = requestWallet(walletId)
        return productV2Repository.consumePurchaseSync(
            walletGenerationModel.walletAddress,
            walletGenerationModel.signature,
            packageName,
            purchaseToken
        )
    }

    fun getSkuDetails(
        packageName: String,
        skus: ArrayList<String>,
        paymentFlow: String? = null
    ): SkuDetailsResponse? {
        logInfo("Getting SkuDetails.")
        return productV2Repository.getSkuDetails(packageName, skus, paymentFlow)
    }
}
