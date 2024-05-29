package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context

class BonusSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun getMaxBonus(): Int = getInt(MAX_BONUS_KEY, 0)

    fun setMaxBonus(value: Int) {
        setInt(MAX_BONUS_KEY, value)
        setLong(MAX_BONUS_TTL_SECONDS_KEY, System.currentTimeMillis() / 1000)
    }

    fun hasSavedBonus(timeInMillis: Long): Boolean {
        val savedTtlInSeconds = sharedPreferences.getLong(MAX_BONUS_TTL_SECONDS_KEY, -1)
        if (sharedPreferences.contains(MAX_BONUS_KEY) && savedTtlInSeconds != -1L) {
            return (timeInMillis / 1000 - savedTtlInSeconds) < TTL_IN_SECONDS
        }
        return false
    }

    private companion object {
        const val TTL_IN_SECONDS = 86400 * 30 //86400 = 24h
        const val MAX_BONUS_KEY = "MAX_BONUS"
        const val MAX_BONUS_TTL_SECONDS_KEY = "MAX_BONUS_TTL"
    }
}
