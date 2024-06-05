package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.Companion.DEFAULT_WEB_PAYMENT_URL_VERSION
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject
import java.util.ArrayList

class PayflowResponseMapper {
  fun map(response: RequestResponse): PayflowMethodResponse {
    WalletUtils.getSdkAnalytics()
      .sendCallBackendPayflowEvent(response.responseCode, response.response)

    if (!isSuccess(response.responseCode) || response.response == null) {
      return PayflowMethodResponse(response.responseCode, arrayListOf())
    }

    val paymentFlowList = runCatching {
      JSONObject(response.response).optJSONObject("payment_methods")
        ?.let { paymentMethodsObject ->
          paymentMethodsObject.keys().asSequence().mapNotNull { methodName: String ->
            val priority = paymentMethodsObject.optJSONObject(methodName)?.optInt("priority") ?: -1
            when (methodName) {
              "wallet" -> PaymentFlowMethod.Wallet(methodName, priority)
              "pay_as_a_guest" -> PaymentFlowMethod.PayAsAGuest(methodName, priority)
              "games_hub_checkout" -> PaymentFlowMethod.GamesHub(methodName, priority)
              "web_payment" -> {
                val paymentMethodsJsonObject = paymentMethodsObject.optJSONObject(methodName)
                val version = paymentMethodsJsonObject
                  ?.optString("version") ?: DEFAULT_WEB_PAYMENT_URL_VERSION
                val paymentFlow = paymentMethodsJsonObject?.optString("payment_flow")
                PaymentFlowMethod.WebPayment(methodName, priority, version, paymentFlow)
              }
              else -> null
            }
          }.toCollection(arrayListOf())
        } ?: arrayListOf()
    }.getOrElse {
      it.printStackTrace()
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
  class PayAsAGuest(name: String, priority: Int) : PaymentFlowMethod(name, priority)
  class GamesHub(name: String, priority: Int) : PaymentFlowMethod(name, priority)
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
