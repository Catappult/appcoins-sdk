package com.appcoins.sdk.billing.listeners

import android.app.Activity
import android.content.Intent
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ID
import com.appcoins.sdk.core.logger.Logger.logInfo
import org.json.JSONObject
import java.io.Serializable
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.ORDER_REFERENCE as ORDER_REFERENCE_EXTRA
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE as RESPONSE_CODE_EXTRA

data class SDKWebResponse(
    val responseCode: Int,
    val purchaseData: PurchaseData? = null,
    val dataSignature: String? = null,
    val orderReference: String? = null
) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.optInt(RESPONSE_CODE),
        PurchaseData(JSONObject(jsonObject.optString(PURCHASE_DATA))),
        jsonObject.optString(DATA_SIGNATURE),
        jsonObject.optString(ORDER_REFERENCE),
    )

    constructor(responseCode: Int) : this(
        responseCode, null, null, null
    )

    fun toSDKPaymentResponse(): SDKPaymentResponse =
        SDKPaymentResponse(
            createActivityResultFromResponseCode(responseCode),
            createPaymentResponseBundle()
        )

    private fun createPaymentResponseBundle() =
        Intent().apply {
            logInfo("Putting RESPONSE_CODE_EXTRA with -> ${responseCode}")
            logInfo("Putting INAPP_PURCHASE_DATA with -> ${purchaseData?.toJson()}")
            logInfo("Putting INAPP_DATA_SIGNATURE with -> ${dataSignature}")
            logInfo("Putting INAPP_PURCHASE_ID with -> ${purchaseData?.purchaseToken}")
            logInfo("Putting ORDER_REFERENCE_EXTRA with -> ${orderReference}")
            putExtra(RESPONSE_CODE_EXTRA, responseCode)
            purchaseData?.toJson()?.let { putExtra(INAPP_PURCHASE_DATA, it) }
            dataSignature?.let { putExtra(INAPP_DATA_SIGNATURE, it) }
            purchaseData?.purchaseToken?.let { putExtra(INAPP_PURCHASE_ID, it) }
            orderReference?.let { putExtra(ORDER_REFERENCE_EXTRA, it) }
        }

    private fun createActivityResultFromResponseCode(responseCode: Int) =
        when (responseCode) {
            0, 2, 3, 4, 5, 6, 7, 8 -> Activity.RESULT_OK
            1 -> Activity.RESULT_CANCELED
            else -> Activity.RESULT_FIRST_USER
        }

    private companion object {
        const val RESPONSE_CODE = "responseCode"
        const val PURCHASE_DATA = "purchaseData"
        const val DATA_SIGNATURE = "dataSignature"
        const val ORDER_REFERENCE = "orderReference"
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
) : Serializable {
    fun toJson(): String =
        """{"orderId":"$orderId","packageName":"$packageName","productId":"$productId","productType":"$productType","purchaseTime":$purchaseTime,"purchaseToken":"$purchaseToken","purchaseState":$purchaseState,"isAutoRenewing":"$isAutoRenewing","developerPayload":"$developerPayload"}"""

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
