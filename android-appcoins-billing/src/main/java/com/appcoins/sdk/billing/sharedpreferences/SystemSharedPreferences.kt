package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context

@Suppress("complexity:TooManyFunctions")
class SystemSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun isDontKeepActivitiesEventSent(): Boolean = getBoolean(DONT_KEEP_ACTIVITIES_EVENT_KEY)

    fun sendDontKeepActivitiesEvent() = setBoolean(DONT_KEEP_ACTIVITIES_EVENT_KEY, true)

    private companion object {
        const val DONT_KEEP_ACTIVITIES_EVENT_KEY = "DONT_KEEP_ACTIVITIES_EVENT"
    }
}
