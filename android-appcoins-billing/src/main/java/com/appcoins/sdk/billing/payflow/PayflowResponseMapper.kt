package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.FeatureType
import com.appcoins.sdk.billing.payflow.models.PayflowMethodResponse
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
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
            return PayflowMethodResponse(response.responseCode, arrayListOf(), null)
        }

        val paymentFlowList = mapPaymentFlowMethods(response)

        val analyticsFlowSeverityLevels: ArrayList<AnalyticsFlowSeverityLevel>? =
            mapAnalyticsFlowSeverityLevels(response)

        return PayflowMethodResponse(response.responseCode, paymentFlowList, analyticsFlowSeverityLevels)
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
                                    FeatureType.values()
                                        .find { featureTypeValue -> featureTypeValue.value == featureInt }
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
}
