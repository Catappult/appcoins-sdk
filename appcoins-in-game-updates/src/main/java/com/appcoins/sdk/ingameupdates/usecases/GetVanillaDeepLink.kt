package com.appcoins.sdk.ingameupdates.usecases

object GetVanillaDeepLink {

    fun invoke(packageName: String): String =
        "aptoidesearch://$packageName"
}
