package com.appcoins.sdk.billing.payflow.models.featureflags

import com.appcoins.sdk.core.logger.Logger.logWarning
import org.json.JSONObject

class LimitPurchaseRequests(
    active: Boolean,
    severityLevel: SeverityLevel? = null,
    val rateLimitCount: Int,
) : FeatureFlag(Feature.LIMIT_PURCHASE_REQUESTS, active, severityLevel) {

    companion object {
        fun fromJsonObject(
            active: Boolean,
            severity: SeverityLevel,
            jsonObject: JSONObject?
        ): LimitPurchaseRequests? =
            runCatching {
                if (jsonObject == null) return null
                val rateLimitCount = jsonObject.optInt("rate_limit_count")

                return LimitPurchaseRequests(active, severity, rateLimitCount)
            }.getOrElse {
                logWarning("Error parsing feature flag: ${it.message}")
                null
            }
    }
}
