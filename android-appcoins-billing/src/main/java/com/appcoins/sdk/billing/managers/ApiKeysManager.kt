package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper

internal object ApiKeysManager {
    private val privateKeysNativeHelper by lazy { PrivateKeysNativeHelper() }

    fun getIndicativeApiKey(): String =
        privateKeysNativeHelper.getApiKey(
            BuildConfig.BUILD_TYPE,
            PrivateKeysNativeHelper.ApiKeys.INDICATIVE_API_KEY.name
        )

    fun getMatomoUrl(): String =
        privateKeysNativeHelper.getApiKey(BuildConfig.BUILD_TYPE, PrivateKeysNativeHelper.ApiKeys.MATOMO_URL.name)
}
