package com.appcoins.sdk.billing.helpers

import android.content.Intent
import android.os.Bundle
import com.appcoins.sdk.billing.SkuDetails
import com.appcoins.sdk.billing.types.SkuType
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.DETAILS_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.GET_SKU_DETAILS_ITEM_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ID_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.KEY_BUY_INTENT
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class AndroidBillingMapperTest {

    @Test
    fun `mapArrayListToBundleSkuDetails should return correctly formed Bundle of SkuDetails`() {
        val result = AndroidBillingMapper.mapArrayListToBundleSkuDetails(arrayListOf("sku"))

        assertEquals(result.getStringArrayList(GET_SKU_DETAILS_ITEM_LIST), arrayListOf("sku"))
    }

    @Test
    fun `mapBundleToHashMapSkuDetails should return correctly formed SkuDetailsResult`() {
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
            putStringArrayList(DETAILS_LIST, arrayListOf(EXAMPLE_SKU_DETAILS.toJsonString()))
        }
        val result = AndroidBillingMapper.mapBundleToHashMapSkuDetails(SkuType.inapp.toString(), bundle)

        assertEquals(result.skuDetailsList.first().toString(), EXAMPLE_SKU_DETAILS.toString())
        assertEquals(result.responseCode, 0)
    }

    @Test
    fun `mapBundleToHashMapSkuDetails should return empty list if no DETAILS_LIST in Bundle`() {
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
        }
        val result = AndroidBillingMapper.mapBundleToHashMapSkuDetails(SkuType.inapp.toString(), bundle)

        assertTrue(result.skuDetailsList.isEmpty())
        assertEquals(result.responseCode, 0)
    }

    @Test
    fun `mapBundleToHashMapGetIntent should return null BuyIntent if no KEY_BUY_INTENT in Bundle`() {
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
        }
        val result = AndroidBillingMapper.mapBundleToHashMapGetIntent(bundle)

        assertNull(result.buyIntent)
        assertEquals(result.responseCode, 0)
    }

    @Test
    fun `mapBundleToHashMapGetIntent should return valid BuyIntent if KEY_BUY_INTENT exists in Bundle`() {
        val intent = Intent().apply {
            putExtra("test_key", "test")
        }
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
            putParcelable(KEY_BUY_INTENT, intent)
        }
        val result = AndroidBillingMapper.mapBundleToHashMapGetIntent(bundle)

        assertEquals(result.buyIntent, intent)
        assertEquals(result.responseCode, 0)
    }

    @Test
    fun `mapPurchases should return empty list if purchaseDataList not present in Bundle`() {
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
        }
        val result = AndroidBillingMapper.mapPurchases(bundle, INAPP_TYPE)

        assertTrue(result.purchasesList.isEmpty())
        assertEquals(result.responseCode, 0)
    }

    @Test
    fun `mapPurchases should return empty list if signatureList not present in Bundle`() {
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
            putStringArrayList(INAPP_PURCHASE_DATA_LIST, arrayListOf())
        }
        val result = AndroidBillingMapper.mapPurchases(bundle, INAPP_TYPE)

        assertTrue(result.purchasesList.isEmpty())
        assertEquals(result.responseCode, 0)
    }

    @Test
    fun `mapPurchases should return empty list if idsList not present in Bundle`() {
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
            putStringArrayList(INAPP_PURCHASE_DATA_LIST, arrayListOf())
            putStringArrayList(INAPP_DATA_SIGNATURE_LIST, arrayListOf())
        }
        val result = AndroidBillingMapper.mapPurchases(bundle, INAPP_TYPE)

        assertTrue(result.purchasesList.isEmpty())
        assertEquals(result.responseCode, 0)
    }

    @Test
    fun `mapPurchases should return empty list if purchaseDataList is empty`() {
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
            putStringArrayList(INAPP_PURCHASE_DATA_LIST, arrayListOf())
            putStringArrayList(INAPP_DATA_SIGNATURE_LIST, arrayListOf())
            putStringArrayList(INAPP_PURCHASE_ID_LIST, arrayListOf())
        }
        val result = AndroidBillingMapper.mapPurchases(bundle, INAPP_TYPE)

        assertTrue(result.purchasesList.isEmpty())
        assertEquals(result.responseCode, 0)
    }

    @Test
    fun `mapPurchases should return correctly formed list with Purchases`() {
        val bundle = Bundle().apply {
            putInt(RESPONSE_CODE, 0)
            putStringArrayList(INAPP_PURCHASE_DATA_LIST, arrayListOf(EXAMPLE_PURCHASE_JSON_DATA))
            putStringArrayList(INAPP_DATA_SIGNATURE_LIST, arrayListOf(EXAMPLE_BASE_64_ENCODED_KEY))
            putStringArrayList(INAPP_PURCHASE_ID_LIST, arrayListOf())
        }
        val result = AndroidBillingMapper.mapPurchases(bundle, INAPP_TYPE)

        assertTrue(result.purchasesList.first().sku.equals("productId"))
        assertEquals(result.responseCode, 0)
    }

    private companion object {
        val INAPP_TYPE = SkuType.inapp.toString()
        const val EXAMPLE_BASE_64_ENCODED_KEY = "dGVzdA=="
        val EXAMPLE_SKU_DETAILS =
            SkuDetails(
                "inapp",
                "sku",
                "inapp",
                "0.0",
                0,
                "€",
                "0.0",
                0,
                "APPC",
                "0.0",
                0,
                "€",
                "title",
                "description",
                "period",
                "trialPeriod",
                "trialPeriodEndDate",
            )

        const val EXAMPLE_PURCHASE_JSON_DATA =
            """{"orderId":"orderId","packageName":"packageName","productId":"productId","productType":"inapp","purchaseTime":1723654956371,"purchaseToken":"purchaseToken","purchaseState":0,"isAutoRenewing":false,"developerPayload":"developerPayload"}"""

        fun SkuDetails.toJsonString() =
            JSONObject().apply {
                put("productId", sku)
                put("type", type)
                put("price", price)
                put("price_currency_code", priceCurrencyCode)
                put("price_amount_micros", priceAmountMicros)
                put("appc_price", appcPrice)
                put("appc_price_currency_code", appcPriceCurrencyCode)
                put("appc_price_amount_micros", appcPriceAmountMicros)
                put("fiat_price", fiatPrice)
                put("fiat_price_currency_code", fiatPriceCurrencyCode)
                put("fiat_price_amount_micros", fiatPriceAmountMicros)
                put("title", title)
                description?.let { put("description", it) }
                period?.let { put("period", it) }
                trialPeriod?.let { put("trial_period", it) }
                trialPeriodEndDate?.let { put("trial_period_end_date", it) }
            }.toString()
    }
}
