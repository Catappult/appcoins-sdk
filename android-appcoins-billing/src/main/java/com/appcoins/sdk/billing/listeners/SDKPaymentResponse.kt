package com.appcoins.sdk.billing.listeners

import android.content.Intent

data class SDKPaymentResponse(
    val resultCode: Int,
    val intent: Intent? = null
)
