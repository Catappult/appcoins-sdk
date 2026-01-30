package com.appcoins.sdk.billing.managers

import com.appcoins.sdk.billing.payflow.models.featureflags.FeatureFlag

object FeatureFlagsManager {
    private val defaultFeatureFlags: List<FeatureFlag> = listOf()
    private var featureFlags: List<FeatureFlag>? = defaultFeatureFlags

    fun setFeatureFlags(newFeatureFlags: List<FeatureFlag>) {
        featureFlags = newFeatureFlags
    }

    fun getFeatureFlag(feature: FeatureFlag.Feature): FeatureFlag? {
        return featureFlags?.find { it.feature == feature }
    }

    fun resetFeatureFlags() {
        featureFlags = defaultFeatureFlags
    }
}
