package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context

class AttributionSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun getWalletId(): String? = getString(WALLET_ID_KEY)
    fun getOemId(): String? = getString(OEM_ID_KEY)
    fun isAttributionComplete(): Boolean = getBoolean(ATTRIBUTION_COMPLETE_KEY)
    fun setWalletId(value: String? = null) = setString(WALLET_ID_KEY, value)
    fun setOemId(value: String? = null) = setString(OEM_ID_KEY, value)
    fun completeAttribution() = setBoolean(ATTRIBUTION_COMPLETE_KEY, true)

    private companion object {
        const val WALLET_ID_KEY = "WALLET_ID"
        const val OEM_ID_KEY = "OEM_ID"
        const val ATTRIBUTION_COMPLETE_KEY = "ATTRIBUTION_COMPLETE"
    }
}
