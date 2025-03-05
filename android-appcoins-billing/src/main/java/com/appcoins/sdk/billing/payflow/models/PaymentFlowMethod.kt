package com.appcoins.sdk.billing.payflow.models

import com.appcoins.sdk.billing.FeatureType
import org.json.JSONObject

sealed class PaymentFlowMethod(
    val name: String,
    val priority: Int,
    val availableFeatures: List<FeatureType>?
) {
    class Wallet(name: String, priority: Int, availableFeatures: List<FeatureType>?) :
        PaymentFlowMethod(name, priority, availableFeatures)

    class GamesHub(name: String, priority: Int, availableFeatures: List<FeatureType>?) :
        PaymentFlowMethod(name, priority, availableFeatures)

    class AptoideGames(name: String, priority: Int, availableFeatures: List<FeatureType>?) :
        PaymentFlowMethod(name, priority, availableFeatures)

    class WebPayment(
        name: String,
        priority: Int,
        availableFeatures: List<FeatureType>?,
        val version: String?,
        val paymentFlow: String?,
        val webViewDetails: WebViewDetails?
    ) : PaymentFlowMethod(name, priority, availableFeatures) {

        companion object {
            fun fromJsonObject(
                paymentMethodsJsonObject: JSONObject?,
                methodName: String,
                priority: Int,
                availableFeatures: List<FeatureType>?
            ): WebPayment {
                val version =
                    paymentMethodsJsonObject
                        ?.optString("version")
                        ?.takeIf { it.isNotEmpty() }
                        ?: DEFAULT_WEB_PAYMENT_URL_VERSION
                val paymentFlow =
                    paymentMethodsJsonObject
                        ?.optString("payment_flow")
                        ?.takeIf { it.isNotEmpty() && it != DEFAULT_PAYMENT_FLOW }

                val webViewDetails: WebViewDetails? =
                    WebViewDetails.fromJsonObject(paymentMethodsJsonObject?.optJSONObject("screen_details"))

                return WebPayment(
                    methodName,
                    priority,
                    availableFeatures,
                    version,
                    paymentFlow,
                    webViewDetails,
                )
            }
        }

        override fun toString(): String =
            "${this.javaClass.name}: [name: $name, " +
                "priority: $priority, " +
                "version: $version, " +
                "paymentFlow: $paymentFlow, " +
                "webViewDetails: $webViewDetails]"

        override fun equals(other: Any?): Boolean {
            if (other != null) {
                if (other::class.java == this::class.java) {
                    other as WebPayment
                    return other.name == name &&
                        other.priority == priority &&
                        other.paymentFlow == paymentFlow &&
                        other.version == version &&
                        other.webViewDetails == webViewDetails
                }
            }
            return false
        }
    }

    class UnavailableBilling(
        name: String,
        priority: Int,
        availableFeatures: List<FeatureType>?,
        val errorMessage: String?
    ) : PaymentFlowMethod(name, priority, availableFeatures) {
        companion object {
            fun fromJsonObject(
                paymentMethodsJsonObject: JSONObject?,
                methodName: String,
                priority: Int,
                availableFeatures: List<FeatureType>?,
            ): UnavailableBilling {
                val message =
                    paymentMethodsJsonObject
                        ?.optString("error_message")
                        ?.takeIf { it.isNotEmpty() }

                return UnavailableBilling(methodName, priority, availableFeatures, message)
            }
        }
    }

    override fun toString(): String =
        "${this.javaClass.name}: [name: $name, priority: $priority]"

    override fun equals(other: Any?): Boolean {
        if (other != null) {
            if (other::class.java == this::class.java) {
                other as PaymentFlowMethod
                return other.name == name && other.priority == priority
            }
        }
        return false
    }

    companion object {
        const val DEFAULT_WEB_PAYMENT_URL_VERSION = "v1"
        const val DEFAULT_PAYMENT_FLOW = "default"

        const val SCREEN_ORIENTATION_PORTRAIT = 1
        const val SCREEN_ORIENTATION_LANDSCAPE = 2

        fun getPaymentUrlVersionFromPayflowMethod(payflowMethodsList: MutableList<PaymentFlowMethod>): String? =
            (payflowMethodsList.firstOrNull { it is WebPayment } as WebPayment?)?.version

        fun getPaymentFlowFromPayflowMethod(payflowMethodsList: MutableList<PaymentFlowMethod>?): String? =
            (payflowMethodsList?.firstOrNull { it is WebPayment } as WebPayment?)?.paymentFlow

        fun getUnavailableBillingMessage(payflowMethodsList: MutableList<PaymentFlowMethod>): String? =
            (payflowMethodsList.firstOrNull { it is UnavailableBilling } as UnavailableBilling?)?.errorMessage
    }
}
