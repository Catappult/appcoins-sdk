package com.appcoins.sdk.billing.listeners

import android.net.Uri
import com.appcoins.sdk.billing.Purchase

data class SDKWebResponse(
    val responseCode: Int?,
    val purchaseToken: String?,
    val orderId: String?,
) {
    constructor(uri: Uri) : this(
        uri.getQueryParameter(RESPONSE_CODE)?.toInt(),
        uri.getQueryParameter(PURCHASE_TOKEN),
        uri.getQueryParameter(ORDER_ID)
    )

    fun toPurchase(): Purchase =
        Purchase(
            orderId,
            IN_APP,
            null,
            null,
            0L,
            0,
            null,
            purchaseToken,
            null,
            null,
            false
        )

    companion object {
        private const val IN_APP = "inapp"
        private const val RESPONSE_CODE = "responseCode"
        private const val PURCHASE_TOKEN = "purchaseToken"
        private const val ORDER_ID = "orderId"
    }
}
