package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class PayflowResponseMapper {
  fun map(response: RequestResponse): List<PaymentFlowMethod> {
    if (!isSuccess(response.responseCode) || response.response == null) return emptyList()
    return runCatching {
      JSONObject(response.response).optJSONObject("payment_methods")
        ?.let { paymentMethodsObject ->
          paymentMethodsObject.keys().asSequence().mapNotNull { methodName: String ->
            val priority =
              paymentMethodsObject.optJSONObject(methodName)?.optInt("priority") ?: -1
            when (methodName) {
              "wallet" -> PaymentFlowMethod.Wallet(methodName, priority)
              "pay_as_a_guest" -> PaymentFlowMethod.PayAsAGuest(methodName, priority)
              "games_hub_checkout" -> PaymentFlowMethod.GamesHub(methodName, priority)
              "first_payment_via_web" -> PaymentFlowMethod.WebFirstPayment(methodName, priority)
              else -> null
            }
          }.toList()
        } ?: emptyList()
    }.getOrElse {
      it.printStackTrace()
      emptyList()
    }
  }
}


sealed class PaymentFlowMethod(val name: String, val priority: Int) {
  class Wallet(name: String, priority: Int) : PaymentFlowMethod(name, priority)
  class PayAsAGuest(name: String, priority: Int) : PaymentFlowMethod(name, priority)
  class GamesHub(name: String, priority: Int) : PaymentFlowMethod(name, priority)
  class WebFirstPayment(name: String, priority: Int) : PaymentFlowMethod(name, priority)
}
