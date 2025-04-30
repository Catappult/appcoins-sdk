package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper
import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper.ApiKeys.INDICATIVE_API_KEY
import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper.ApiKeys.MATOMO_API_KEY

internal object ApiKeysManager {
    private val privateKeysNativeHelper by lazy { PrivateKeysNativeHelper() }

    fun getIndicativeApiKey(): String =
        privateKeysNativeHelper.getApiKey(BuildConfig.BUILD_TYPE, INDICATIVE_API_KEY.name)

    fun getMatomoApiKey(): String =
        privateKeysNativeHelper.getApiKey(BuildConfig.BUILD_TYPE, MATOMO_API_KEY.name)
}
