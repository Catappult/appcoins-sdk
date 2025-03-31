package com.appcoins.sdk.billing.helpers

internal class PrivateKeysNativeHelper {
    external fun getApiKey(buildType: String, key: String): String

    companion object {
        init {
            System.loadLibrary("native-keys-storer")
        }
    }

    enum class ApiKeys {
        INDICATIVE_API_KEY,
    }
}
