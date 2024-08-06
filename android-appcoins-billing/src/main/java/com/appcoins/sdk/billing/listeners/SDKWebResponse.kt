package com.appcoins.sdk.billing.listeners

import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.Purchase
import org.json.JSONObject

data class SDKWebResponse(
    val responseCode: Int?,
    val purchaseToken: String? = null,
    val orderId: String? = null,
) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.optInt(RESPONSE_CODE),
        jsonObject.optString(PURCHASE_TOKEN),
        jsonObject.optString(ORDER_ID)
    )

    fun toPurchase(billingFlowParams: BillingFlowParams? = null): Purchase =
        Purchase(
            orderId,
            billingFlowParams?.skuType,
            null,
            null,
            0L,
            0,
            generateDeveloperPayloadFromBillingFlowParams(billingFlowParams),
            purchaseToken,
            null,
            billingFlowParams?.sku,
            false
        )

    private fun generateDeveloperPayloadFromBillingFlowParams(
        billingFlowParams: BillingFlowParams?
    ): String {
        if (!billingFlowParams?.developerPayload.isNullOrEmpty()) {
            return billingFlowParams!!.developerPayload
        }

        if (!billingFlowParams?.orderReference.isNullOrEmpty()) {
            return billingFlowParams!!.orderReference
        }

        return ""
    }

    private companion object {
        const val RESPONSE_CODE = "responseCode"
        const val PURCHASE_TOKEN = "purchaseToken"
        const val ORDER_ID = "orderId"
    }
}
