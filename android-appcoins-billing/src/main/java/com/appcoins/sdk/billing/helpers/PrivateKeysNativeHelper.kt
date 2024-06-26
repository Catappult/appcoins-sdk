package com.appcoins.sdk.billing.helpers

internal class PrivateKeysNativeHelper {
    external fun getIndicativeApiKey(): String

    companion object{
        init {
            System.loadLibrary("native-keys-storer")
        }
    }
}