package com.appcoins.sdk.billing.webpayment

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class WebPaymentResponseMapper {
    fun map(response: RequestResponse): WebPaymentResponse {
        WalletUtils.getSdkAnalytics()
            .sendCallBackendWebPaymentUrlEvent(response.responseCode, response.response)

        if (!isSuccess(response.responseCode) || response.response == null) {
            return WebPaymentResponse(response.responseCode)
        }

        val webPaymentUrl = runCatching {
            JSONObject(response.response).getString("payment_url")
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
