package com.appcoins.sdk.billing.managers

import com.appcoins.sdk.billing.payflow.models.featureflags.LimitPurchaseRequests

object LimitPurchaseRequestsManager {
    private var currentPurchaseRequestsCount = 0
    private var limitPurchaseRequests: LimitPurchaseRequests? = null

    fun onPurchaseInitiated() {
        if (limitPurchaseRequests?.active != true) {
            return
        }
        resetPurchaseRequestsCount()
    }

    fun onPurchasesRequestMade() {
        if (limitPurchaseRequests?.active != true) {
            return
        }
        currentPurchaseRequestsCount++
    }

    fun resetPurchaseRequestsCount() {
        currentPurchaseRequestsCount = 0
    }

    fun canMakePurchaseRequest(): Boolean {
        if (limitPurchaseRequests?.active != true) {
            return true
        }
        val rateLimitCount = limitPurchaseRequests?.rateLimitCount ?: return true
        return currentPurchaseRequestsCount < rateLimitCount
    }

    fun updateRequestsLimits(limitPurchaseRequests: LimitPurchaseRequests?) {
        if (limitPurchaseRequests == null) {
            this.limitPurchaseRequests = null
            return
        }
        this.limitPurchaseRequests = limitPurchaseRequests
    }
}
