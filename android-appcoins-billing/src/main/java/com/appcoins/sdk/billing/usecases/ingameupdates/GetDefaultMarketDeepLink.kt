package com.appcoins.sdk.billing.usecases.ingameupdates

import com.appcoins.sdk.billing.usecases.UseCase

object GetDefaultMarketDeepLink : UseCase() {
    operator fun invoke(packageName: String): String {
        super.invokeUseCase()
        return "market://details?id=$packageName"
    }
}
