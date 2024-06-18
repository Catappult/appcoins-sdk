package com.appcoins.sdk.billing.usecases.ingameupdates

object GetVanillaDeepLink {

    fun invoke(packageName: String): String =
        "aptoidesearch://$packageName"
}
