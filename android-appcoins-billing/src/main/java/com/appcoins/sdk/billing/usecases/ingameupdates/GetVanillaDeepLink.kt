package com.appcoins.sdk.billing.usecases.ingameupdates

import com.appcoins.sdk.billing.usecases.UseCase

object GetVanillaDeepLink : UseCase() {

    operator fun invoke(packageName: String): String {
        super.invokeUseCase()
        return "aptoidesearch://$packageName"
    }
}
