package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject
import java.io.Serializable

class PayflowResponseMapper {
    fun map(response: RequestResponse): PayflowMethodResponse {
        WalletUtils.sdkAnalytics.sendCallBackendPayflowEvent(
            response.responseCode,
            response.response,
            response.exception?.toString()
        )

        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain Payflow Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
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
                            "web_payment" ->
                                PaymentFlowMethod.WebPayment.fromJsonObject(
                                    paymentMethodsObject.optJSONObject(methodName),
                                    methodName,
                                    priority
                                )

                            else -> null
                        }
                    }.toCollection(arrayListOf())
                } ?: arrayListOf()
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
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
) {
    class Wallet(name: String, priority: Int) : PaymentFlowMethod(name, priority)
    class GamesHub(name: String, priority: Int) : PaymentFlowMethod(name, priority)
    class AptoideGames(name: String, priority: Int) : PaymentFlowMethod(name, priority)
    class WebPayment(
        name: String,
        priority: Int,
        val version: String?,
        val paymentFlow: String?,
        val webViewDetails: WebViewDetails?
    ) : PaymentFlowMethod(name, priority) {

        data class WebViewDetails(
            var forcedScreenOrientation: Int? = null,
            var landscapeScreenDimensions: OrientedScreenDimensions? = null,
            var portraitScreenDimensions: OrientedScreenDimensions? = null
        ) : Serializable {

            fun hasLandscapeDetails(): Boolean =
                landscapeScreenDimensions != null &&
                    (
                        landscapeScreenDimensions?.heightDp != null ||
                            landscapeScreenDimensions?.heightPercentage != null
                        ) &&
                    (landscapeScreenDimensions?.widthDp != null || landscapeScreenDimensions?.widthPercentage != null)

            fun hasPortraitDetails(): Boolean =
                portraitScreenDimensions != null &&
                    (
                        portraitScreenDimensions?.heightDp != null ||
                            portraitScreenDimensions?.heightPercentage != null
                        ) &&
                    (portraitScreenDimensions?.widthDp != null || portraitScreenDimensions?.widthPercentage != null)

            data class OrientedScreenDimensions(
                var widthDp: Int?,
                var heightDp: Int?,
                var widthPercentage: Double?,
                var heightPercentage: Double?,
            ) : Serializable
        }

        companion object {
            fun fromJsonObject(paymentMethodsJsonObject: JSONObject?, methodName: String, priority: Int): WebPayment {
                val version =
                    paymentMethodsJsonObject
                        ?.optString("version")
                        ?.takeIf { it.isNotEmpty() }
                        ?: DEFAULT_WEB_PAYMENT_URL_VERSION
                val paymentFlow =
                    paymentMethodsJsonObject
                        ?.optString("payment_flow")
                        ?.takeIf { it.isNotEmpty() && it != DEFAULT_PAYMENT_FLOW }

                var webViewDetails: WebViewDetails? = null
                paymentMethodsJsonObject?.optJSONObject("screen_details")?.let { screenDetailsJSONObject ->
                    val forcedScreenOrientation =
                        screenDetailsJSONObject.optInt("force_screen_orientation").takeIf { it != 0 }

                    var landscapeScreenDimensions: WebViewDetails.OrientedScreenDimensions? = null
                    screenDetailsJSONObject.optJSONObject("landscape")?.let { jsonObject ->
                        val widthDp = jsonObject.optInt("width_dp").takeIf { it != 0 }
                        val heightDp = jsonObject.optInt("height_dp").takeIf { it != 0 }
                        val widthPercentage =
                            jsonObject.optDouble("width_percentage").takeIf { !it.isNaN() }
                        val heightPercentage =
                            jsonObject.optDouble("height_percentage")
                                .takeIf { !it.isNaN() }
                        landscapeScreenDimensions =
                            WebViewDetails.OrientedScreenDimensions(
                                widthDp,
                                heightDp,
                                widthPercentage,
                                heightPercentage
                            )
                    }

                    var portraitScreenDimensions: WebViewDetails.OrientedScreenDimensions? = null
                    screenDetailsJSONObject.optJSONObject("portrait")?.let { jsonObject ->
                        val widthDp = jsonObject.optInt("width_dp").takeIf { it != 0 }
                        val heightDp = jsonObject.optInt("height_dp").takeIf { it != 0 }
                        val widthPercentage =
                            jsonObject.optDouble("width_percentage").takeIf { !it.isNaN() }
                        val heightPercentage =
                            jsonObject.optDouble("height_percentage")
                                .takeIf { !it.isNaN() }
                        portraitScreenDimensions =
                            WebViewDetails.OrientedScreenDimensions(
                                widthDp,
                                heightDp,
                                widthPercentage,
                                heightPercentage
                            )
                    }
                    webViewDetails =
                        WebViewDetails(forcedScreenOrientation, landscapeScreenDimensions, portraitScreenDimensions)
                }

                return WebPayment(
                    methodName,
                    priority,
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
    }
}
