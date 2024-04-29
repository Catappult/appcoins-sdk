package com.appcoins.sdk.billing.listeners

import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.Purchase
import org.json.JSONObject

data class SDKWebResponse(
    val responseCode: Int?,
    val purchaseToken: String?,
    val orderId: String?,
) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.optInt(RESPONSE_CODE),
        jsonObject.optString(PURCHASE_TOKEN),
        jsonObject.optString(ORDER_ID)
    )

    fun toPurchase(
        billingFlowParams: BillingFlowParams? = null,
        developerPayload: String? = null
    ): Purchase =
        Purchase(
            orderId,
            billingFlowParams?.skuType,
            null,
            null,
            0L,
            0,
            developerPayload,
            purchaseToken,
            null,
            billingFlowParams?.sku,
            false
        )

    companion object {
        private const val RESPONSE_CODE = "responseCode"
        private const val PURCHASE_TOKEN = "purchaseToken"
        private const val ORDER_ID = "orderId"
    }
}
