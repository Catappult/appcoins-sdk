package com.appcoins.sdk.billing.payflow.models.featureflags

import com.appcoins.sdk.core.logger.Logger.logWarning
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
    enum class Feature(val value: String) {
        MMP_PURCHASE_RESILIENCE("mmp_purchase_resilience"),
        LIMIT_SDK_REQUESTS("limit_sdk_requests"),
        LIMIT_PURCHASE_REQUESTS("limit_purchase_requests"),
    }

    companion object {
        fun fromJsonObject(jsonObject: JSONObject): FeatureFlag? =
            runCatching {
                val feature = jsonObject.optString("feature")

                val active = jsonObject.optBoolean("active")
                val severityLevel = jsonObject.optInt("severity_level")
                val severity = SeverityLevel.entries.find { it.value == severityLevel } ?: SeverityLevel.NONE
                val customFieldsJSONObject = jsonObject.optJSONObject("custom_fields")

                return when (feature) {
                    Feature.MMP_PURCHASE_RESILIENCE.value -> MMPPurchaseResilience(active, severity)
                    Feature.LIMIT_SDK_REQUESTS.value ->
                        LimitSDKRequests.fromJsonObject(active, severity, customFieldsJSONObject)

                    Feature.LIMIT_PURCHASE_REQUESTS.value ->
                        LimitPurchaseRequests.fromJsonObject(active, severity, customFieldsJSONObject)

                    else -> null
                }
            }.getOrElse {
                logWarning("Error parsing feature flag: ${it.message}")
                null
            }
    }
}
