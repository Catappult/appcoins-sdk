package com.appcoins.sdk.core.analytics.matomo.models

data class MatomoDetails(
    val matomoCustomProperties: ArrayList<CustomProperty>?,
    val matomoUrl: String?,
    val matomoApiKey: String?,
)
