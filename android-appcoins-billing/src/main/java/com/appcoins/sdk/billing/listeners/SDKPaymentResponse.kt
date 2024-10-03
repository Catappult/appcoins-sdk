package com.appcoins.sdk.billing.listeners

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE

data class SDKPaymentResponse(
    val resultCode: Int,
    val intent: Intent? = null
) {
    companion object {
        fun createCanceledTypeResponse() =
            SDKPaymentResponse(
                RESULT_CANCELED,
                Intent().apply { putExtra(RESPONSE_CODE, ResponseCode.USER_CANCELED.value) }
            )

        fun createErrorTypeResponse() =
            SDKPaymentResponse(
                RESULT_OK,
                Intent().apply { putExtra(RESPONSE_CODE, ResponseCode.ERROR.value) }
            )
    }
}
