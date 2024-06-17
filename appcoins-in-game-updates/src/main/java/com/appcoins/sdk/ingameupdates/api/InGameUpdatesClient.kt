package com.appcoins.sdk.ingameupdates.api

interface InGameUpdatesClient {
    fun isAppUpdateAvailable(): Boolean
    fun launchAppUpdateFlow()
}
