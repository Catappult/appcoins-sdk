package com.appcoins.sdk.core.logger

import com.appcoins.sdk.core.security.PurchasesSecurityHelper
import com.appcoins.sdk.core.security.Security
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class PurchasesSecurityHelperTest {

    @Before
    fun setup() {
        PurchasesSecurityHelper.base64DecodedPublicKey = BASE_64_DECODED_PUBLIC_KEY
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

        val result = PurchasesSecurityHelper.verifyPurchase(purchaseData, BASE_64_DECODED_PUBLIC_KEY)

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

        val result = PurchasesSecurityHelper.verifyPurchase(purchaseData, BASE_64_DECODED_PUBLIC_KEY)

        assertFalse(result)
    }

    private companion object {
        val BASE_64_DECODED_PUBLIC_KEY = ByteArray(1)
    }
}
