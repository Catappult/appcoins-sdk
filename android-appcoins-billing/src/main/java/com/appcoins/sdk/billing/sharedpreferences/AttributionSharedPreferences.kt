package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context

class AttributionSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun getWalletId(): String? = getString(WALLET_ID_KEY)
    fun getOemId(): String? = getString(OEM_ID_KEY)
    fun setWalletId(value: String? = null) = setString(WALLET_ID_KEY, value)
    fun setOemId(value: String? = null) = setString(OEM_ID_KEY, value)

    private companion object {
        const val WALLET_ID_KEY = "WALLET_ID"
        const val OEM_ID_KEY = "OEM_ID"
    }
}
