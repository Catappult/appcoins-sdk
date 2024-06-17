package com.appcoins.sdk.ingameupdates.usecases

object GetDefaultMarketDeepLink {
    fun invoke(packageName: String): String =
        "market://details?id=$packageName"
}
