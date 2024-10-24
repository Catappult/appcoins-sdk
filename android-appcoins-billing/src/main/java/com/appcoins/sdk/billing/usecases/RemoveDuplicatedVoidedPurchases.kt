package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.VoidedPurchasesResult

object RemoveDuplicatedVoidedPurchases : UseCase() {
    operator fun invoke(voidedPurchasesResult: VoidedPurchasesResult) {
        super.invokeUseCase()

        voidedPurchasesResult.voidedPurchases =
            voidedPurchasesResult.voidedPurchases.distinctBy { it.purchaseToken }
    }
}
