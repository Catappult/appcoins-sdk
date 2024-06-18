package com.appcoins.sdk.billing.usecases.ingameupdates

object GetDefaultMarketDeepLink {
    fun invoke(packageName: String): String =
        "market://details?id=$packageName"
}
