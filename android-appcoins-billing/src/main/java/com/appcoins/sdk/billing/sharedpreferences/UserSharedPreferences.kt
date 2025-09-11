package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context

@Suppress("complexity:TooManyFunctions")
class UserSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun saveSessionStartTime(value: Long) = setLong(SESSION_START_TIME_KEY, value)

    fun getSessionStartTime(): Long = getLong(SESSION_START_TIME_KEY)

    fun saveSessionEndTime(value: Long) = setLong(SESSION_END_TIME_KEY, value)

    fun getSessionEndTime(): Long = getLong(SESSION_END_TIME_KEY)

    private companion object {
        const val SESSION_START_TIME_KEY = "SESSION_START_TIME"
        const val SESSION_END_TIME_KEY = "SESSION_END_TIME"
    }
}
