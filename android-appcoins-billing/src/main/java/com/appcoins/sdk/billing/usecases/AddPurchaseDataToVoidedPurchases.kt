package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.VoidedPurchaseData
import com.appcoins.sdk.billing.VoidedPurchasesResult
import com.appcoins.sdk.billing.managers.ProductV2Manager

object AddPurchaseDataToVoidedPurchases : UseCase() {
    operator fun invoke(voidedPurchasesResult: VoidedPurchasesResult) {
        super.invokeUseCase()

        voidedPurchasesResult.voidedPurchases.forEach { voidedPurchase ->
            val inAppPurchase = ProductV2Manager.getInappPurchase(voidedPurchase.purchaseToken)
            inAppPurchase?.let {
                voidedPurchase.voidedPurchaseData = VoidedPurchaseData(it.sku, it.payload)
            }
        }
    }
}
