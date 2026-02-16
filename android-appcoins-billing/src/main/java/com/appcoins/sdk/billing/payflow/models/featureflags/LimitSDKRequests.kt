package com.appcoins.sdk.billing.payflow.models.featureflags

import com.appcoins.sdk.core.extensions.forEachObject
import com.appcoins.sdk.core.logger.Logger.logWarning
import org.json.JSONObject

class LimitSDKRequests(
    active: Boolean,
    severityLevel: SeverityLevel? = null,
    val limitDetailsList: ArrayList<LimitDetails>? = null
) : FeatureFlag(Feature.LIMIT_SDK_REQUESTS, active, severityLevel) {

    class LimitDetails(
        val sdkRequestTypes: ArrayList<SDKRequestType>,
        val rateLimitCount: Int,
        val rateLimitDuration: Int,
        val cooldownDuration: Int,
    )

    enum class SDKRequestType(val value: String) {
        GET_PRODUCT_DETAILS("get_product_details"),
        GET_PURCHASES("get_purchases"),
        CONSUME_PURCHASE("consume_purchase"),
    }

    companion object {
        fun fromJsonObject(
            active: Boolean,
            severity: SeverityLevel,
            jsonObject: JSONObject?
        ): LimitSDKRequests? = runCatching {
            if (jsonObject == null) return null

            val limitDetailsListJsonArray = jsonObject.optJSONArray("limit_details_list") ?: return null

            val limitDetailsList = arrayListOf<LimitDetails>()

            limitDetailsListJsonArray.forEachObject { limitDetailsJson ->
                val sdkRequestTypesJson = limitDetailsJson.optJSONArray("sdk_request_types") ?: return@forEachObject

                val sdkRequestTypesList = (0 until sdkRequestTypesJson.length()).mapNotNull { index ->
                    val typeString = sdkRequestTypesJson.optString(index)
                    runCatching { SDKRequestType.valueOf(typeString.uppercase()) }.getOrNull()
                }

                if (sdkRequestTypesList.isNotEmpty()) {
                    limitDetailsList.add(
                        LimitDetails(
                            sdkRequestTypes = sdkRequestTypesList as ArrayList<SDKRequestType>,
                            rateLimitCount = limitDetailsJson.optInt("rate_limit_count"),
                            rateLimitDuration = limitDetailsJson.optInt("rate_limit_duration"),
                            cooldownDuration = limitDetailsJson.optInt("cooldown_duration")
                        )
                    )
                }
            }

            if (limitDetailsList.isEmpty()) {
                return null
            }

            return LimitSDKRequests(active, severity, limitDetailsList)
        }.getOrElse {
            logWarning("Error parsing feature flag: ${it.message}")
            null
        }
    }
}
