package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.FeatureType
import com.appcoins.sdk.billing.payflow.models.PayflowMethodResponse
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod
import com.appcoins.sdk.billing.payflow.models.featureflags.FeatureFlag
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.analytics.matomo.models.CustomProperty
import com.appcoins.sdk.core.analytics.matomo.models.MatomoDetails
import com.appcoins.sdk.core.analytics.severity.AnalyticsFlowSeverityLevel
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class PayflowResponseMapper {
    fun map(response: RequestResponse): PayflowMethodResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Payflow Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return PayflowMethodResponse(
                response.responseCode,
                arrayListOf(),
                null,
                null,
                null
            )
        }

        val paymentFlowList = mapPaymentFlowMethods(response)
        val analyticsFlowSeverityLevels = mapAnalyticsFlowSeverityLevels(response)
        val matomoDetails = mapMatomoDetails(response)
        val featureFlags = mapFeatureFlags(response)

        return PayflowMethodResponse(
            response.responseCode,
            paymentFlowList,
            analyticsFlowSeverityLevels,
            matomoDetails,
            featureFlags
        )
    }

    private fun mapPaymentFlowMethods(response: RequestResponse): ArrayList<PaymentFlowMethod> =
        runCatching {
            JSONObject(response.response).optJSONObject("payment_methods")
                ?.let { paymentMethodsObject ->
                    paymentMethodsObject.keys().asSequence().mapNotNull { methodName: String ->
                        val priority =
                            paymentMethodsObject.optJSONObject(methodName)?.optInt("priority") ?: -1
                        val availableFeatures =
                            paymentMethodsObject.optJSONObject(methodName)?.optJSONArray("supported_features")?.let {
                                val arrayList = arrayListOf<Int>()
                                for (i in 0 until it.length()) {
                                    arrayList.add(it.optInt(i))
                                }
                                arrayList.mapNotNull { featureInt ->
                                    FeatureType.entries.find { featureTypeValue ->
                                        featureTypeValue.value == featureInt
                                    }
                                }
                            }
                        when (methodName) {
                            "wallet" -> PaymentFlowMethod.Wallet(methodName, priority, availableFeatures)
                            "games_hub_checkout" -> PaymentFlowMethod.GamesHub(methodName, priority, availableFeatures)
                            "aptoide_games" -> PaymentFlowMethod.AptoideGames(methodName, priority, availableFeatures)
                            "web_payment" ->
                                PaymentFlowMethod.WebPayment.fromJsonObject(
                                    paymentMethodsObject.optJSONObject(methodName),
                                    methodName,
                                    priority,
                                    availableFeatures
                                )

                            "unavailable_billing" ->
                                PaymentFlowMethod.UnavailableBilling.fromJsonObject(
                                    paymentMethodsObject.optJSONObject(methodName),
                                    methodName,
                                    priority,
                                    availableFeatures
                                )

                            else -> null
                        }
                    }.toCollection(arrayListOf())
                } ?: arrayListOf()
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.PAYMENT_FLOW,
                response.response,
                Exception(it).toString()
            )
            arrayListOf()
        }

    private fun mapAnalyticsFlowSeverityLevels(response: RequestResponse): ArrayList<AnalyticsFlowSeverityLevel>? =
        runCatching {
            JSONObject(response.response).optJSONArray("analytics_flow_severity_levels")
                ?.let { analyticsFlowSeverityLevelsJsonArray ->
                    val analyticsFlowSeverityLevels = arrayListOf<AnalyticsFlowSeverityLevel>()
                    for (i in 0 until analyticsFlowSeverityLevelsJsonArray.length()) {
                        val analyticsFlowSeverityLevelJsonObject =
                            analyticsFlowSeverityLevelsJsonArray.optJSONObject(i)
                        analyticsFlowSeverityLevels.add(
                            AnalyticsFlowSeverityLevel(
                                analyticsFlowSeverityLevelJsonObject.optString("flow"),
                                analyticsFlowSeverityLevelJsonObject.optInt("severity_level"),
                            )
                        )
                    }
                    analyticsFlowSeverityLevels
                }
        }.getOrElse {
            logError("There was an error mapping the AnalyticsFlowSeverityLevels.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.PAYMENT_FLOW,
                response.response,
                Exception(it).toString()
            )
            null
        }

    private fun mapFeatureFlags(response: RequestResponse): ArrayList<FeatureFlag>? =
        runCatching {
            JSONObject(response.response).optJSONArray("feature_flags")
                ?.let { featuresFlagsJsonArray ->
                    val featuresFlags = arrayListOf<FeatureFlag>()
                    for (i in 0 until featuresFlagsJsonArray.length()) {
                        val featuresFlagJsonObject = featuresFlagsJsonArray.optJSONObject(i)
                        FeatureFlag.fromJsonObject(featuresFlagJsonObject)?.let {
                            featuresFlags.add(it)
                        }
                    }
                    featuresFlags
                }
        }.getOrElse {
            logError("There was an error mapping the FeatureFlags.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.PAYMENT_FLOW,
                response.response,
                Exception(it).toString()
            )
            null
        }

    private fun mapMatomoDetails(response: RequestResponse): MatomoDetails? =
        runCatching {
            JSONObject(response.response).optJSONObject("matomo_details")?.let { matomoDetailsJsonObject ->
                MatomoDetails(
                    mapMatomoCustomProperties(matomoDetailsJsonObject),
                    getMatomoUrl(matomoDetailsJsonObject),
                    getMatomoApiKey(matomoDetailsJsonObject),
                )
            }
        }.getOrElse {
            logError("There was an error mapping the MatomoDetails.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.PAYMENT_FLOW,
                response.response,
                Exception(it).toString()
            )
            null
        }

    private fun mapMatomoCustomProperties(matomoDetailsJsonObject: JSONObject): ArrayList<CustomProperty>? =
        runCatching {
            matomoDetailsJsonObject.optJSONArray("matomo_custom_properties")
                ?.let { analyticsCustomPropertiesJsonArray ->
                    val analyticsCustomProperties = arrayListOf<CustomProperty>()
                    for (i in 0 until analyticsCustomPropertiesJsonArray.length()) {
                        val analyticsCustomPropertiesJsonObject =
                            analyticsCustomPropertiesJsonArray.optJSONObject(i)
                        analyticsCustomProperties.add(
                            CustomProperty(
                                analyticsCustomPropertiesJsonObject.optInt("sdk_id"),
                                analyticsCustomPropertiesJsonObject.optInt("matomo_id"),
                            )
                        )
                    }
                    analyticsCustomProperties
                }
        }.getOrElse {
            logError("There was an error mapping the AnalyticsFlowSeverityLevels.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.PAYMENT_FLOW,
                matomoDetailsJsonObject.toString(),
                Exception(it).toString()
            )
            null
        }

    private fun getMatomoUrl(matomoDetailsJsonObject: JSONObject): String? =
        runCatching {
            matomoDetailsJsonObject.optString("matomo_url").takeIf { it.isNotEmpty() }
        }.getOrElse {
            logError("There was an error mapping the MatomoUrl.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.PAYMENT_FLOW,
                matomoDetailsJsonObject.toString(),
                Exception(it).toString()
            )
            null
        }

    private fun getMatomoApiKey(matomoDetailsJsonObject: JSONObject): String? =
        runCatching {
            matomoDetailsJsonObject.optString("matomo_api_key").takeIf { it.isNotEmpty() }
        }.getOrElse {
            logError("There was an error mapping the MatomoApiKey.", Exception(it))
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.PAYMENT_FLOW,
                matomoDetailsJsonObject.toString(),
                Exception(it).toString()
            )
            null
        }
}
