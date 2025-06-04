package com.appcoins.sdk.billing.helpers

import com.appcoins.sdk.billing.BillingResult

object BillingResultHelper {

    @JvmStatic
    fun buildBillingResult(responseCode: Int, errorType: Int? = null): BillingResult {
        return BillingResult.newBuilder()
            .setResponseCode(responseCode)
            .apply {
                errorType?.let {
                    when (errorType) {
                        ERROR_TYPE_INVALID_PUBLIC_KEY -> setDebugMessage("Invalid public key")
                        ERROR_TYPE_MAIN_THREAD -> setDebugMessage("Request from MainThread. Cancelling.")
                        ERROR_TYPE_SERVICE_NOT_AVAILABLE -> setDebugMessage("Service not available")
                        else -> setDebugMessage("Unknown error")
                    }
                }
            }
            .build()
    }

    const val ERROR_TYPE_INVALID_PUBLIC_KEY = 0
    const val ERROR_TYPE_MAIN_THREAD = 1
    const val ERROR_TYPE_SERVICE_NOT_AVAILABLE = 2
}
