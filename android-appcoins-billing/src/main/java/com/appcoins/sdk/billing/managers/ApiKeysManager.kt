package com.appcoins.sdk.billing.managers

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper
import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper.ApiKeys.ADYEN_API_KEY
import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper.ApiKeys.INDICATIVE_API_KEY
import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper.ApiKeys.RAKAM_API_KEY

internal object ApiKeysManager {
    private val privateKeysNativeHelper = PrivateKeysNativeHelper()

    fun getIndicativeApiKey(): String =
        privateKeysNativeHelper.getApiKey(BuildConfig.BUILD_TYPE, INDICATIVE_API_KEY.name)

    fun getRakamApiKey(): String =
        privateKeysNativeHelper.getApiKey(BuildConfig.BUILD_TYPE, RAKAM_API_KEY.name)

    fun getAdyenApiKey(): String =
        privateKeysNativeHelper.getApiKey(BuildConfig.BUILD_TYPE, ADYEN_API_KEY.name)
}
