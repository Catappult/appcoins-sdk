package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.mappers.PurchaseResponse
import com.appcoins.sdk.billing.repositories.ProductV2Repository
import com.appcoins.sdk.billing.service.BdsService

object ProductV2Manager {
    private val productV2Repository = ProductV2Repository(BdsService(BuildConfig.HOST_WS, 3000))

    fun getPurchase(purchaseToken: String): PurchaseResponse? =
        productV2Repository.getPurchase(purchaseToken)
}
