package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.Companion.DEFAULT_PAYMENT_FLOW
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.Companion.DEFAULT_WEB_PAYMENT_URL_VERSION
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class PayflowResponseMapper {
    fun map(response: RequestResponse): PayflowMethodResponse {
        WalletUtils.getSdkAnalytics()
            .sendCallBackendPayflowEvent(
                response.responseCode,
                response.response,
                response.exception?.toString()
            )

        if (!isSuccess(response.responseCode) || response.response == null) {
            logError("Failed to obtain Payflow Response. ResponseCode: ${response.responseCode} | Cause: ${response.exception}")
            return PayflowMethodResponse(response.responseCode, arrayListOf())
        }

        val paymentFlowList = runCatching {
            JSONObject(response.response).optJSONObject("payment_methods")
                ?.let { paymentMethodsObject ->
                    paymentMethodsObject.keys().asSequence().mapNotNull { methodName: String ->
                        val priority =
                            paymentMethodsObject.optJSONObject(methodName)?.optInt("priority") ?: -1
                        when (methodName) {
                            "wallet" -> PaymentFlowMethod.Wallet(methodName, priority)
                            "games_hub_checkout" -> PaymentFlowMethod.GamesHub(methodName, priority)
                            "aptoide_games" -> PaymentFlowMethod.AptoideGames(methodName, priority)
                            "web_payment" -> {
                                val paymentMethodsJsonObject =
                                    paymentMethodsObject.optJSONObject(methodName)
                                val version =
                                    paymentMethodsJsonObject
                                        ?.optString("version")
                                        ?.takeIf { it.isNotEmpty() }
                                        ?: DEFAULT_WEB_PAYMENT_URL_VERSION
                                val paymentFlow =
                                    paymentMethodsJsonObject
                                        ?.optString("payment_flow")
                                        ?.takeIf { it.isNotEmpty() && it != DEFAULT_PAYMENT_FLOW }
                                PaymentFlowMethod.WebPayment(
                                    methodName,
                                    priority,
                                    version,
                                    paymentFlow
                                )
                            }

                            else -> null
                        }
                    }.toCollection(arrayListOf())
                } ?: arrayListOf()
        }.getOrElse {
            logError("There was a an error mapping the response.", Exception(it))
            arrayListOf()
        }
        return PayflowMethodResponse(response.responseCode, paymentFlowList)
    }
}

data class PayflowMethodResponse(
    val responseCode: Int?,
    val paymentFlowList: ArrayList<PaymentFlowMethod>?
)

sealed class PaymentFlowMethod(
    val name: String,
    val priority: Int,
    val version: String? = null,
    val paymentFlow: String? = null,
) {
    class Wallet(name: String, priority: Int) : PaymentFlowMethod(name, priority)
    class GamesHub(name: String, priority: Int) : PaymentFlowMethod(name, priority)
    class AptoideGames(name: String, priority: Int) : PaymentFlowMethod(name, priority)
    class WebPayment(name: String, priority: Int, version: String?, paymentFlow: String?) :
        PaymentFlowMethod(name, priority, version, paymentFlow)

    override fun equals(other: Any?): Boolean {
        if (other != null) {
            if (other::class.java == this::class.java) {
                other as PaymentFlowMethod
                return other.paymentFlow == paymentFlow
                    && other.name == name
                    && other.priority == priority
                    && other.version == version
            }
        }
        return false
    }

    companion object {
        const val DEFAULT_WEB_PAYMENT_URL_VERSION = "v1"
        const val DEFAULT_PAYMENT_FLOW = "default"

        fun getPaymentUrlVersionFromPayflowMethod(
            payflowMethodsList: MutableList<PaymentFlowMethod>
        ): String? =
            payflowMethodsList.firstOrNull { it is WebPayment }?.version

        fun getPaymentFlowFromPayflowMethod(
            payflowMethodsList: MutableList<PaymentFlowMethod>
        ): String? =
            payflowMethodsList.firstOrNull { it is WebPayment }?.paymentFlow
    }
}
