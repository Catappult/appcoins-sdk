package com.appcoins.sdk.billing.models

enum class ResponseType(val value: Int) {
    /**
     * External Payment
     */
    EXTERNAL_PAYMENT(0);

    companion object {
        fun fromValue(value: Int?): ResponseType? =
            ResponseType.values().firstOrNull { it.value == value }
    }
}
