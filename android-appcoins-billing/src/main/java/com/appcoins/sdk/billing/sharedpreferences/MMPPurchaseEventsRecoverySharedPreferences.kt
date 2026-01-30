package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context

class MMPPurchaseEventsRecoverySharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun addPurchaseLaunchedCount() {
        val currentCount = getInt(PURCHASE_LAUNCHED_COUNT_KEY)
        if (currentCount == 0) {
            setLastPurchaseTime(System.currentTimeMillis())
        }
        setInt(PURCHASE_LAUNCHED_COUNT_KEY, currentCount + 1)
    }

    fun getPurchaseLaunchedCount(): Int = getInt(PURCHASE_LAUNCHED_COUNT_KEY)

    fun getLastPurchaseTime(): Long = getLong(LAST_PURCHASE_TIME_KEY)

    fun setLastPurchaseTime(time: Long) {
        setLong(LAST_PURCHASE_TIME_KEY, time)
    }

    fun resetPurchaseLaunchedCount() {
        setInt(PURCHASE_LAUNCHED_COUNT_KEY, 0)
    }

    fun resetLastPurchaseTime() {
        setLong(LAST_PURCHASE_TIME_KEY, 0)
    }

    fun decreasePurchaseLaunchedCount() {
        val currentCount = getPurchaseLaunchedCount()

        if (currentCount > 0) {
            if (currentCount == 1) {
                resetPurchaseLaunchedCount()
                resetLastPurchaseTime()
            } else {
                setInt(PURCHASE_LAUNCHED_COUNT_KEY, currentCount - 1)
            }
        }
    }

    fun getPurchasesEventSent(): Set<String> =
        getStringSet(PURCHASES_EVENT_SENT_KEY) ?: emptySet()

    fun addPurchaseEventSent(purchaseId: String) {
        val currentSet = getPurchasesEventSent().toMutableSet()
        currentSet.add(purchaseId)
        setStringSet(PURCHASES_EVENT_SENT_KEY, currentSet)
    }

    fun resetPurchasesEventSent() {
        setStringSet(PURCHASES_EVENT_SENT_KEY, emptySet())
    }

    private companion object {
        const val PURCHASES_EVENT_SENT_KEY = "PURCHASES_EVENT_SENT"
        const val PURCHASE_LAUNCHED_COUNT_KEY = "PURCHASE_LAUNCHED_COUNT"
        const val LAST_PURCHASE_TIME_KEY = "LAST_PURCHASE_TIME"
    }
}
