package com.appcoins.sdk.billing.managers

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.payflow.models.featureflags.MMPPurchaseResilience
import com.appcoins.sdk.billing.sharedpreferences.MMPPurchaseEventsRecoverySharedPreferences
import com.appcoins.sdk.billing.usecases.mmp.SendSuccessfulPurchaseResponseEvent
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.logger.Logger.logWarning

object MMPPurchaseEventsRecoveryManager {

    private val mmpPurchaseEventsRecoverySharedPreferences by lazy {
        MMPPurchaseEventsRecoverySharedPreferences(WalletUtils.context)
    }
    private var isMMPEventResilienceFeatureSupported = false

    fun onPurchaseInitiated() = runCatching {
        if (!isMMPEventResilienceFeatureSupported) {
            return@runCatching
        }
        mmpPurchaseEventsRecoverySharedPreferences.addPurchaseLaunchedCount()
    }.getOrElse {
        logWarning("Error handling purchase initiated: ${it.message}")
    }

    fun onPurchaseCompleted(purchase: Purchase) = runCatching {
        if (!isMMPEventResilienceFeatureSupported) {
            return@runCatching
        }
        mmpPurchaseEventsRecoverySharedPreferences.addPurchaseEventSent(purchase.orderId)
        mmpPurchaseEventsRecoverySharedPreferences.decreasePurchaseLaunchedCount()
    }.getOrElse {
        logWarning("Error handling purchase completed: ${it.message}")
    }

    fun verifyMissingMMPEvents() = runCatching {
        Thread {
            if (!isMMPEventResilienceFeatureSupported) {
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
                        transaction.uid?.let { uid ->
                            SdkAnalyticsUtils.sdkAnalytics.sendMMPPurchaseEventRecovered(uid)
                        }
                    }
                }
            }
            mmpPurchaseEventsRecoverySharedPreferences.resetLastPurchaseTime()
            mmpPurchaseEventsRecoverySharedPreferences.resetPurchaseLaunchedCount()
            mmpPurchaseEventsRecoverySharedPreferences.resetPurchasesEventSent()
        }.start()
    }.getOrElse {
        logWarning("Error verifying missing MMP events: ${it.message}")
    }

    fun updateRecoveryState(mmpPurchaseResilience: MMPPurchaseResilience?) {
        isMMPEventResilienceFeatureSupported = mmpPurchaseResilience?.active ?: false
    }
}
