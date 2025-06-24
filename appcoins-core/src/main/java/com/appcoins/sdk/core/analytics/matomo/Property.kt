package com.appcoins.sdk.core.analytics.matomo

interface Property {
    val key: String
    val eventName: String
    val id: Int
}
