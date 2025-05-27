package com.appcoins.sdk.billing

import android.content.Intent
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException
import com.appcoins.sdk.core.security.Security
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class AppCoinsBillingTest {

    private lateinit var appCoinsBilling: AppCoinsBilling

    private val mockkRepository = mockk<Repository>()

    @Before
    fun setup() {
        appCoinsBilling = AppCoinsBilling(mockkRepository)
    }

    @Test
    fun `verifyPurchase should return true if Signature was verified correctly`() {
        val purchaseData = "empty_data"

        mockkStatic(Security::class)
        every {
            Security.verifyPurchase(
                BASE_64_DECODED_PUBLIC_KEY,
                purchaseData,
                BASE_64_DECODED_PUBLIC_KEY
            )
        } returns true

        val result = appCoinsBilling.verifyPurchase(purchaseData, BASE_64_DECODED_PUBLIC_KEY)

        assertTrue(result)
    }

    @Test
    fun `verifyPurchase should return false if Signature was verified incorrectly`() {
        val purchaseData = "empty_data"

        mockkStatic(Security::class)
        every {
            Security.verifyPurchase(
                BASE_64_DECODED_PUBLIC_KEY,
                purchaseData,
                BASE_64_DECODED_PUBLIC_KEY
            )
        } returns false

        val result = appCoinsBilling.verifyPurchase(purchaseData, BASE_64_DECODED_PUBLIC_KEY)

        assertFalse(result)
    }

    @Test
    fun `queryPurchases should return OK on PurchasesResult if successful`() {
        val skuType = "skuType"

        mockkStatic(Security::class)
        every {
            Security.verifyPurchase(
                BASE_64_DECODED_PUBLIC_KEY,
                any(),
                any()
            )
        } returns true
        every { mockkRepository.getPurchases(skuType) } returns SUCCESSFUL_PURCHASES_RESULT

        val result = appCoinsBilling.queryPurchases(skuType)

        assertEquals(ResponseCode.OK.value, result.responseCode)
        assertTrue(result.purchases.isNotEmpty())
    }

    @Test
    fun `queryPurchases should return ERROR on PurchasesResult if verification failed`() {
        val skuType = "skuType"

        mockkStatic(Security::class)
        every {
            Security.verifyPurchase(
                BASE_64_DECODED_PUBLIC_KEY,
                any(),
                any()
            )
        } returns false
        every { mockkRepository.getPurchases(skuType) } returns SUCCESSFUL_PURCHASES_RESULT

        val result = appCoinsBilling.queryPurchases(skuType)

        assertEquals(ResponseCode.ERROR.value, result.responseCode)
        assertTrue(result.purchases.isEmpty())
    }

    @Test
    fun `queryPurchases should return SERVICE_UNAVAILABLE on PurchasesResult if repository throws ServiceConnectionException`() {
        val skuType = "skuType"

        every { mockkRepository.getPurchases(skuType) } throws ServiceConnectionException()

        val result = appCoinsBilling.queryPurchases(skuType)

        assertEquals(ResponseCode.SERVICE_UNAVAILABLE.value, result.responseCode)
        assertTrue(result.purchases.isEmpty())
    }

    @Test
    fun `queryPurchases should return same responseCode of PurchasesResult if response not successful`() {
        val skuType = "skuType"

        every { mockkRepository.getPurchases(skuType) } returns DEVELOPER_ERROR_PURCHASES_RESULT

        val result = appCoinsBilling.queryPurchases(skuType)

        assertEquals(ResponseCode.DEVELOPER_ERROR.value, result.responseCode)
        assertTrue(result.purchases.isEmpty())
    }

    @Test
    fun `launchBillingFlow should return OK on LaunchBillingFlowResult if successful`() {
        val payload = "payload"
        val oemid = "oemid"
        val guestWalletId = "guestWalletId"

        every {
            mockkRepository.launchBillingFlow(
                BILLING_FLOW_PARAMS.skuType,
                BILLING_FLOW_PARAMS.sku,
                payload,
                oemid,
                guestWalletId
            )
        } returns SUCCESSFUL_LAUNCH_BILLING_FLOW_RESULT

        val result = appCoinsBilling.launchBillingFlow(BILLING_FLOW_PARAMS, payload, oemid, guestWalletId)

        assertEquals(ResponseCode.OK.value, result.responseCode)
    }

    @Test(expected = ServiceConnectionException::class)
    fun `launchBillingFlow should return SERVICE_UNAVAILABLE on LaunchBillingFlowResult if repository throws ServiceConnectionException`() {
        val payload = "payload"
        val oemid = "oemid"
        val guestWalletId = "guestWalletId"

        every {
            mockkRepository.launchBillingFlow(
                BILLING_FLOW_PARAMS.skuType,
                BILLING_FLOW_PARAMS.sku,
                payload,
                oemid,
                guestWalletId
            )
        } throws ServiceConnectionException()

        appCoinsBilling.launchBillingFlow(BILLING_FLOW_PARAMS, payload, oemid, guestWalletId)
    }

    @Test
    fun `launchBillingFlow should return same responseCode of LaunchBillingFlowResult if response not successful`() {
        val payload = "payload"
        val oemid = "oemid"
        val guestWalletId = "guestWalletId"

        every {
            mockkRepository.launchBillingFlow(
                BILLING_FLOW_PARAMS.skuType,
                BILLING_FLOW_PARAMS.sku,
                payload,
                oemid,
                guestWalletId
            )
        } returns DEVELOPER_ERROR_LAUNCH_BILLING_FLOW_RESULT

        val result = appCoinsBilling.launchBillingFlow(BILLING_FLOW_PARAMS, payload, oemid, guestWalletId)

        assertEquals(ResponseCode.DEVELOPER_ERROR.value, result.responseCode)
    }

    @Test
    fun `isReady should return true if repository is ready`() {
        every { mockkRepository.isReady } returns true

        val result = appCoinsBilling.isReady

        assertTrue(result)
    }

    @Test
    fun `isReady should return false if repository is not ready`() {
        every { mockkRepository.isReady } returns false

        val result = appCoinsBilling.isReady

        assertFalse(result)
    }

    private companion object {
        val BASE_64_DECODED_PUBLIC_KEY = ByteArray(1)

        val PURCHASES_LIST = listOf(
            Purchase(
                "orderId",
                "itemType",
                "originalJson",
                byteArrayOf(),
                0,
                0,
                "developerPayload",
                "obfuscatedAccountId",
                "token",
                "packageName",
                "sku",
                false
            )
        )
        val SUCCESSFUL_PURCHASES_RESULT = PurchasesResult(PURCHASES_LIST, ResponseCode.OK.value)
        val DEVELOPER_ERROR_PURCHASES_RESULT = PurchasesResult(PURCHASES_LIST, ResponseCode.DEVELOPER_ERROR.value)

        val BILLING_FLOW_PARAMS =
            BillingFlowParams("sku", "inapp", "order123", "user12345", "BDS", "user12345", true)
        val SUCCESSFUL_LAUNCH_BILLING_FLOW_RESULT = LaunchBillingFlowResult(ResponseCode.OK.value, Intent())
        val DEVELOPER_ERROR_LAUNCH_BILLING_FLOW_RESULT =
            LaunchBillingFlowResult(ResponseCode.DEVELOPER_ERROR.value, Intent())
    }
}
