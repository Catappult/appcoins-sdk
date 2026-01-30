package com.appcoins.sdk.billing.managers

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.sharedpreferences.MMPPurchaseEventsRecoverySharedPreferences
import com.appcoins.sdk.billing.usecases.mmp.IsMMPEventResilienceFeatureSupported
import com.appcoins.sdk.billing.usecases.mmp.SendSuccessfulPurchaseResponseEvent

object MMPPurchaseEventsRecoveryManager {

    private val mmpPurchaseEventsRecoverySharedPreferences by lazy {
        MMPPurchaseEventsRecoverySharedPreferences(WalletUtils.context)
    }

    fun onPurchaseInitiated() {
        if (!IsMMPEventResilienceFeatureSupported()) {
            return
        }
        mmpPurchaseEventsRecoverySharedPreferences.addPurchaseLaunchedCount()
    }

    fun onPurchaseCompleted(purchase: Purchase) {
        if (!IsMMPEventResilienceFeatureSupported()) {
            return
        }
        mmpPurchaseEventsRecoverySharedPreferences.addPurchaseEventSent(purchase.orderId)
        mmpPurchaseEventsRecoverySharedPreferences.decreasePurchaseLaunchedCount()
    }

    fun verifyMissingMMPEvents() {
        Thread {
            if (!IsMMPEventResilienceFeatureSupported()) {
                return@Thread
            }
            val lastPurchaseTime = mmpPurchaseEventsRecoverySharedPreferences.getLastPurchaseTime()

            if (lastPurchaseTime == 0L) {
                return@Thread
            }

            val lastUserPurchases = BrokerManager.getIapTransactionsFromTimestamp(lastPurchaseTime)
            val sentPurchaseEvents = mmpPurchaseEventsRecoverySharedPreferences.getPurchasesEventSent()
            lastUserPurchases?.transactions?.let {
                it.forEach { transaction ->
                    if (!sentPurchaseEvents.contains(transaction.uid)) {
                        SendSuccessfulPurchaseResponseEvent(transaction = transaction)
                    }
                }
            }
            mmpPurchaseEventsRecoverySharedPreferences.resetLastPurchaseTime()
            mmpPurchaseEventsRecoverySharedPreferences.resetPurchaseLaunchedCount()
            mmpPurchaseEventsRecoverySharedPreferences.resetPurchasesEventSent()
        }.start()
    }
}
