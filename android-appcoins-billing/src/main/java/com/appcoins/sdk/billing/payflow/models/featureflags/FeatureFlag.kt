package com.appcoins.sdk.billing.payflow.models.featureflags

import org.json.JSONObject

sealed class FeatureFlag(
    val feature: Feature,
    val active: Boolean,
    val severityLevel: SeverityLevel? = null
) {
    enum class SeverityLevel(val value: Int) {
        NONE(0),
        LOW(1),
        LOW_MEDIUM(2),
        MEDIUM(3),
        HIGH_MEDIUM(4),
        HIGH(5),
    }

    @Suppress("MagicNumber")
    enum class Feature(val value: Int) {
        MMP_PURCHASE_RESILIENCE(1000),
    }

    class MMPPurchaseResilience(
        active: Boolean,
        severityLevel: SeverityLevel? = null
    ) : FeatureFlag(Feature.MMP_PURCHASE_RESILIENCE, active, severityLevel)

    companion object {
        fun fromJsonObject(jsonObject: JSONObject): FeatureFlag? {
            val feature = jsonObject.optInt("feature").takeUnless { it == 0 }

            val active = jsonObject.optBoolean("active")
            val severityString = jsonObject.optString("severity_level")
            val severity = SeverityLevel.entries.find { it.name == severityString } ?: SeverityLevel.NONE

            return when (feature) {
                Feature.MMP_PURCHASE_RESILIENCE.value -> MMPPurchaseResilience(active, severity)
                else -> null
            }
        }
    }
}
