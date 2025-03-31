package com.appcoins.sdk.billing.models

enum class ResponseType(val value: Int) {
    /**
     * External Payment
     */
    EXTERNAL_PAYMENT(0),

    /**
     * Web Payment Action
     */
    WEB_PAYMENT_ACTION(1);

    companion object {
        fun fromValue(value: Int?): ResponseType? =
            ResponseType.values().firstOrNull { it.value == value }
    }
}
