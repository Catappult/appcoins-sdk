package com.appcoins.sdk.billing.webpayment

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class WebPaymentResponseMapper {
    fun map(response: RequestResponse): WebPaymentResponse {
        /*WalletUtils.getSdkAnalytics()
            .sendCallBackendPayflowEvent(response.responseCode, response.response)*/

        if (!isSuccess(response.responseCode) || response.response == null) {
            return WebPaymentResponse(response.responseCode)
        }

        val webPaymentUrl = runCatching {
            JSONObject(response.response).optString("payment_url")
        }.getOrElse {
            it.printStackTrace()
            null
        }
        return WebPaymentResponse(response.responseCode, webPaymentUrl)
    }
}

data class WebPaymentResponse(
    val responseCode: Int?,
    val webPaymentUrl: String? = null
)
