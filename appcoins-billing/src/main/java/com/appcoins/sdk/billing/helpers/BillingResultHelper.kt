package com.appcoins.sdk.billing.helpers

object BillingResultHelper {

    @JvmStatic
    fun getMessageFromErrorType(errorType: Int?): String? =
        errorType?.let {
            when (errorType) {
                ERROR_TYPE_INVALID_PUBLIC_KEY -> "Invalid public key."
                ERROR_TYPE_MAIN_THREAD -> "Request from MainThread. Cancelling."
                ERROR_TYPE_SERVICE_NOT_AVAILABLE -> "Service not available."
                ERROR_TYPE_PURCHASE_TOKEN_CANNOT_BE_NULL ->
                    "Purchase token cannot be null or empty."

                ERROR_TYPE_INVALID_PRODUCT_TYPE -> "Invalid product type."
                else -> "Unknown error"
            }
        }

    const val ERROR_TYPE_INVALID_PUBLIC_KEY = 0
    const val ERROR_TYPE_MAIN_THREAD = 1
    const val ERROR_TYPE_SERVICE_NOT_AVAILABLE = 2
    const val ERROR_TYPE_PURCHASE_TOKEN_CANNOT_BE_NULL = 3
    const val ERROR_TYPE_INVALID_PRODUCT_TYPE = 4
}
