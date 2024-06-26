package com.appcoins.sdk.billing.managers

import com.appcoins.sdk.billing.helpers.PrivateKeysNativeHelper

internal object ApiKeysManager {
    private val privateKeysNativeHelper = PrivateKeysNativeHelper()

    fun getIndicativeApiKey(): String =
        privateKeysNativeHelper.getIndicativeApiKey()
}
