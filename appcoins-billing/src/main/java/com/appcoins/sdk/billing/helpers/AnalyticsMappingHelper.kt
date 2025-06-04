package com.appcoins.sdk.billing.helpers

import com.appcoins.sdk.billing.PurchasesResult
import com.appcoins.sdk.billing.SkuDetailsResult

class AnalyticsMappingHelper {
    fun mapPurchasesToListOfStrings(purchasesResult: PurchasesResult): List<String> =
        purchasesResult.purchasesList.map { it.token ?: it.orderId }

    fun mapSkuDetailsToListOfStrings(skuDetailsResult: SkuDetailsResult): List<String> =
        skuDetailsResult.skuDetailsList.map { it.sku }
}
