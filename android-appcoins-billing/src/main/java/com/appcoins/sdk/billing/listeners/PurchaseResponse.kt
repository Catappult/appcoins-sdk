package com.appcoins.sdk.billing.listeners

import com.appcoins.sdk.billing.Purchase

data class PurchaseResponse(val responseCode: Int, val purchases: List<Purchase>)
