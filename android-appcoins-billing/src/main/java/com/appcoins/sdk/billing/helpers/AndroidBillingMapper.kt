package com.appcoins.sdk.billing.helpers

import android.os.Bundle
import android.util.Base64
import com.appcoins.sdk.billing.LaunchBillingFlowResult
import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.PurchasesResult
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.SkuDetails
import com.appcoins.sdk.billing.SkuDetailsResult
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.DETAILS_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.GET_SKU_DETAILS_ITEM_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ID_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.KEY_BUY_INTENT
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONException
import org.json.JSONObject

object AndroidBillingMapper {
    @Suppress("NestedBlockDepth")
    @JvmStatic
    fun mapPurchases(bundle: Bundle, skuType: String): PurchasesResult {
        val responseCode = bundle.getInt(RESPONSE_CODE)
        val list: MutableList<Purchase> = ArrayList()
        val listPurchaseTokens: MutableList<String> = ArrayList()
        val purchaseDataList = bundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST)
        val signatureList = bundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST)
        val idsList = bundle.getStringArrayList(INAPP_PURCHASE_ID_LIST)

        if (purchaseDataList != null && signatureList != null && idsList != null) {
            for (i in purchaseDataList.indices) {
                val purchaseData = purchaseDataList[i]
                val signature = signatureList[i]

                try {
                    val jsonElement = JSONObject(purchaseData)
                    val orderId = jsonElement.getString("orderId")
                    val packageName = jsonElement.getString("packageName")
                    val sku = jsonElement.getString("productId")
                    val purchaseTime = jsonElement.getLong("purchaseTime")
                    val purchaseState = jsonElement.getInt("purchaseState")

                    val developerPayload = getStringValueFromJson(jsonElement, "developerPayload", null)
                    val obfuscatedAccountId =
                        getStringValueFromJson(jsonElement, "obfuscatedExternalAccountId", null)
                    var token = getStringValueFromJson(jsonElement, "token", null)
                    if (token == null) {
                        token = getStringValueFromJson(jsonElement, "purchaseToken", null)
                    }
                    val isAutoRenewing = getBooleanValueFromJson(jsonElement, "autoRenewing")

                    // Base64 decoded string
                    val decodedSignature = Base64.decode(signature, Base64.DEFAULT)

                    if (!listPurchaseTokens.contains(token!!)) {
                        listPurchaseTokens.add(token)
                        list.add(
                            Purchase(
                                orderId,
                                skuType,
                                purchaseData,
                                decodedSignature,
                                purchaseTime,
                                purchaseState,
                                developerPayload,
                                obfuscatedAccountId,
                                token,
                                packageName,
                                sku,
                                isAutoRenewing
                            )
                        )
                    }
                } catch (e: JSONException) {
                    logError("Failed to map Purchase: $e")
                }
            }
        }
        return PurchasesResult(list, responseCode)
    }

    @JvmStatic
    fun mapArrayListToBundleSkuDetails(skus: List<String?>?): Bundle {
        val bundle = Bundle()
        bundle.putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, skus as ArrayList<String?>?)
        return bundle
    }

    @Suppress("NestedBlockDepth")
    @JvmStatic
    fun mapBundleToHashMapSkuDetails(skuType: String, bundle: Bundle): SkuDetailsResult {
        val arrayList = ArrayList<SkuDetails>()

        if (bundle.containsKey(DETAILS_LIST)) {
            val responseList = bundle.getStringArrayList(DETAILS_LIST)
            if (responseList != null) {
                for (value in responseList) {
                    val skuDetails = parseSkuDetails(skuType, value)
                    if (skuDetails != null) {
                        arrayList.add(skuDetails)
                    }
                }
            }
        }

        var responseCode = ResponseCode.ERROR.value
        if (bundle.containsKey(RESPONSE_CODE)) {
            responseCode = bundle[RESPONSE_CODE] as Int
        }

        return SkuDetailsResult(arrayList, responseCode)
    }

    @JvmStatic
    fun mapBundleToHashMapGetIntent(bundle: Bundle): LaunchBillingFlowResult {
        return LaunchBillingFlowResult(bundle.getInt(RESPONSE_CODE), bundle.getParcelable(KEY_BUY_INTENT))
    }

    private fun parseSkuDetails(skuType: String, skuDetailsData: String): SkuDetails? {
        try {
            val jsonElement = JSONObject(skuDetailsData)

            val sku = jsonElement.getString("productId")
            val type = jsonElement.getString("type")
            val price = jsonElement.getString("price")
            val priceAmountMicros = jsonElement.getLong("price_amount_micros")
            val priceCurrencyCode = jsonElement.getString("price_currency_code")
            val appcPrice = jsonElement.getString("appc_price")
            val appcPriceAmountMicros = jsonElement.getLong("appc_price_amount_micros")
            val appcPriceCurrencyCode = jsonElement.getString("appc_price_currency_code")
            val fiatPrice = jsonElement.getString("fiat_price")
            val fiatPriceAmountMicros = jsonElement.getLong("fiat_price_amount_micros")
            val fiatPriceCurrencyCode = jsonElement.getString("fiat_price_currency_code")
            val title = jsonElement.getString("title")
            val description = getStringValueFromJson(jsonElement, "description", null)
            val period = getStringValueFromJson(jsonElement, "period", null)
            val trialPeriod = getStringValueFromJson(jsonElement, "trial_period", null)
            val trialPeriodEndDate = getStringValueFromJson(jsonElement, "trial_period_end_date", null)

            return SkuDetails(
                skuType,
                sku,
                type,
                price,
                priceAmountMicros,
                priceCurrencyCode,
                appcPrice,
                appcPriceAmountMicros,
                appcPriceCurrencyCode,
                fiatPrice,
                fiatPriceAmountMicros,
                fiatPriceCurrencyCode,
                title,
                description,
                period,
                trialPeriod,
                trialPeriodEndDate
            )
        } catch (e: JSONException) {
            logError("Failed to parse SkuDetails: $e")
        }

        return null
    }

    private fun getStringValueFromJson(jsonObject: JSONObject, key: String, defaultValue: String?): String? {
        var value: String? = null
        try {
            if (jsonObject.has(key)) {
                value = jsonObject.getString(key)
            }
        } catch (e: JSONException) {
            logDebug("Field error" + e.localizedMessage)
        }
        if (value == null) {
            value = defaultValue
        }
        return value
    }

    private fun getBooleanValueFromJson(jsonObject: JSONObject, key: String): Boolean {
        var value = false
        try {
            if (jsonObject.has(key)) {
                value = jsonObject.getBoolean(key)
            }
        } catch (e: JSONException) {
            logDebug("Field error" + e.localizedMessage)
        }
        return value
    }
}
