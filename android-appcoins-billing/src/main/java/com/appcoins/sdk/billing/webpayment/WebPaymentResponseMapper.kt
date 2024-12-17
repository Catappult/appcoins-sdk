package com.appcoins.sdk.billing.webpayment

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class WebPaymentResponseMapper {
    fun map(response: RequestResponse): WebPaymentResponse {
        WalletUtils.sdkAnalytics.sendCallBackendWebPaymentUrlEvent(
            response.responseCode,
            response.response,
            response.exception?.toString()
        )

        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain WebPaymentUrl Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return WebPaymentResponse(response.responseCode)
        }

        val webPaymentUrl = runCatching {
            JSONObject(response.response).getString("payment_url")
        }.getOrElse {
            logError("There was an error mapping the response.", Exception(it))
            null
        }
        return WebPaymentResponse(response.responseCode, webPaymentUrl)
    }
}

data class WebPaymentResponse(
    val responseCode: Int?,
    val webPaymentUrl: String? = null
)
