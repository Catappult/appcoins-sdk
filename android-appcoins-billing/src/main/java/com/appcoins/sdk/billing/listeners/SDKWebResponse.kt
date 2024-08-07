package com.appcoins.sdk.billing.listeners

import com.appcoins.sdk.billing.Purchase
import org.json.JSONObject

data class SDKWebResponse(
    val responseCode: Int,
    val purchaseData: PurchaseData? = null,
    val dataSignature: String? = null,
    val orderReference: String? = null
) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.optInt(RESPONSE_CODE),
        PurchaseData(jsonObject.optJSONObject(PURCHASE_DATA) ?: JSONObject()),
        jsonObject.optString(DATA_SIGNATURE),
        jsonObject.optString(ORDER_REFERENCE),
    )

    constructor(responseCode: Int) : this(
        responseCode, null, null, null
    )

    fun toPurchase(): Purchase =
        Purchase(
            purchaseData?.orderId,
            purchaseData?.productType,
            toOriginalJson(),
            dataSignature?.toByteArray(),
            purchaseData?.purchaseTime ?: 0,
            purchaseData?.purchaseState ?: 6,
            purchaseData?.developerPayload,
            purchaseData?.purchaseToken,
            purchaseData?.packageName,
            purchaseData?.productId,
            purchaseData?.isAutoRenewing ?: false
        )

    private companion object {
        const val RESPONSE_CODE = "responseCode"
        const val PURCHASE_DATA = "purchaseData"
        const val DATA_SIGNATURE = "dataSignature"
        const val ORDER_REFERENCE = "orderReference"
        fun toOriginalJson(): String =
            ""
    }
}

data class PurchaseData(
    val orderId: String,
    val packageName: String,
    val productId: String,
    val productType: String,
    val purchaseTime: Long,
    val purchaseToken: String,
    val purchaseState: Int,
    val isAutoRenewing: Boolean,
    val developerPayload: String?
) {

    constructor(jsonObject: JSONObject) : this(
        jsonObject.optString(ORDER_ID),
        jsonObject.optString(PACKAGE_NAME),
        jsonObject.optString(PRODUCT_ID),
        jsonObject.optString(PRODUCT_TYPE),
        jsonObject.optLong(PURCHASE_TIME),
        jsonObject.optString(PURCHASE_TOKEN),
        jsonObject.optInt(PURCHASE_STATE),
        jsonObject.optBoolean(IS_AUTO_RENEWING),
        jsonObject.optString(DEVELOPER_PAYLOAD)
    )

    private companion object {
        const val ORDER_ID = "orderId"
        const val PACKAGE_NAME = "packageName"
        const val PRODUCT_ID = "productId"
        const val PRODUCT_TYPE = "productType"
        const val PURCHASE_TIME = "purchaseTime"
        const val PURCHASE_TOKEN = "purchaseToken"
        const val PURCHASE_STATE = "purchaseState"
        const val IS_AUTO_RENEWING = "isAutoRenewing"
        const val DEVELOPER_PAYLOAD = "developerPayload"
    }
}
