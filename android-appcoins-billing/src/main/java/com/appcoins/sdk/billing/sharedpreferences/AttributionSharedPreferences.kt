package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context

@Suppress("complexity:TooManyFunctions")
class AttributionSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun getWalletId(): String? = getString(WALLET_ID_KEY)
    fun getOemId(): String? = getString(OEM_ID_KEY)
    fun getUtmSource(): String? = getString(UTM_SOURCE_KEY)
    fun getUtmMedium(): String? = getString(UTM_MEDIUM_KEY)
    fun getUtmCampaign(): String? = getString(UTM_CAMPAIGN_KEY)
    fun getUtmTerm(): String? = getString(UTM_TERM_KEY)
    fun getUtmContent(): String? = getString(UTM_CONTENT_KEY)
    fun isAttributionComplete(): Boolean = getBoolean(ATTRIBUTION_COMPLETE_KEY)

    fun setWalletId(value: String? = null) = setString(WALLET_ID_KEY, value)
    fun setOemId(value: String? = null) = setString(OEM_ID_KEY, value)
    fun setUtmSource(value: String? = null) = setString(UTM_SOURCE_KEY, value)
    fun setUtmMedium(value: String? = null) = setString(UTM_MEDIUM_KEY, value)
    fun setUtmCampaign(value: String? = null) = setString(UTM_CAMPAIGN_KEY, value)
    fun setUtmTerm(value: String? = null) = setString(UTM_TERM_KEY, value)
    fun setUtmContent(value: String? = null) = setString(UTM_CONTENT_KEY, value)
    fun completeAttribution() = setBoolean(ATTRIBUTION_COMPLETE_KEY, true)

    private companion object {
        const val WALLET_ID_KEY = "WALLET_ID"
        const val OEM_ID_KEY = "OEM_ID"
        const val UTM_SOURCE_KEY = "UTM_SOURCE"
        const val UTM_MEDIUM_KEY = "UTM_MEDIUM"
        const val UTM_CAMPAIGN_KEY = "UTM_CAMPAIGN"
        const val UTM_TERM_KEY = "UTM_TERM"
        const val UTM_CONTENT_KEY = "UTM_CONTENT"
        const val ATTRIBUTION_COMPLETE_KEY = "ATTRIBUTION_COMPLETE"
    }
}
