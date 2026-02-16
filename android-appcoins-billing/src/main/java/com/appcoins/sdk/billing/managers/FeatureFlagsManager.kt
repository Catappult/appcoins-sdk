package com.appcoins.sdk.billing.managers

import com.appcoins.sdk.billing.payflow.models.featureflags.FeatureFlag
import com.appcoins.sdk.billing.payflow.models.featureflags.LimitPurchaseRequests
import com.appcoins.sdk.billing.payflow.models.featureflags.LimitSDKRequests
import com.appcoins.sdk.billing.payflow.models.featureflags.MMPPurchaseResilience

object FeatureFlagsManager {
    private val defaultFeatureFlags: List<FeatureFlag> = listOf()
    private var featureFlags: List<FeatureFlag>? = defaultFeatureFlags

    fun setFeatureFlags(newFeatureFlags: List<FeatureFlag>) {
        featureFlags = newFeatureFlags
        updateFeatureFlagManagers()
    }

    fun resetFeatureFlags() {
        featureFlags = defaultFeatureFlags
        updateFeatureFlagManagers()
    }

    private fun updateFeatureFlagManagers() {
        LimitSDKRequestsManager.updateRequestsLimits(
            limitSDKRequests = getFeatureFlag(
                FeatureFlag.Feature.LIMIT_SDK_REQUESTS
            ) as LimitSDKRequests?
        )
        MMPPurchaseEventsRecoveryManager.updateRecoveryState(
            mmpPurchaseResilience = getFeatureFlag(
                FeatureFlag.Feature.MMP_PURCHASE_RESILIENCE
            ) as MMPPurchaseResilience?
        )
        LimitPurchaseRequestsManager.updateRequestsLimits(
            limitPurchaseRequests = getFeatureFlag(
                FeatureFlag.Feature.LIMIT_PURCHASE_REQUESTS
            ) as LimitPurchaseRequests?
        )
    }

    private fun getFeatureFlag(feature: FeatureFlag.Feature): FeatureFlag? {
        return featureFlags?.find { it.feature == feature }
    }
}
