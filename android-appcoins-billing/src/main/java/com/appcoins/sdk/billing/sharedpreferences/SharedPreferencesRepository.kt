package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

@Suppress("complexity:TooManyFunctions")
abstract class SharedPreferencesRepository(context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getString(key: String, defaultValue: String? = null): String? =
        sharedPreferences.getString(key, defaultValue)

    fun getInt(key: String, defaultValue: Int = 0): Int =
        sharedPreferences.getInt(key, defaultValue)

    fun getLong(key: String, defaultValue: Long = 0): Long =
        sharedPreferences.getLong(key, defaultValue)

    fun getFloat(key: String, defaultValue: Float = 0f): Float =
        sharedPreferences.getFloat(key, defaultValue)

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    fun setString(key: String, value: String? = null) =
        sharedPreferences.edit().putString(key, value).apply()

    fun setInt(key: String, value: Int) =
        sharedPreferences.edit().putInt(key, value).apply()

    fun setLong(key: String, value: Long) =
        sharedPreferences.edit().putLong(key, value).apply()

    fun setFloat(key: String, value: Float) =
        sharedPreferences.edit().putFloat(key, value).apply()

    fun setBoolean(key: String, value: Boolean) =
        sharedPreferences.edit().putBoolean(key, value).apply()

    fun remove(key: String) =
        sharedPreferences.edit().remove(key).apply()
}
