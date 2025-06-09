package com.appcoins.sdk.billing.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import com.appcoins.sdk.billing.CatapultAppcoinsBilling
import com.appcoins.sdk.billing.PurchasesUpdatedListener
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class CatapultBillingAppCoinsFactoryTest {

    private val mockkContext = mockk<Context>()
    private val mockkPurchasesUpdatedListener = mockk<PurchasesUpdatedListener>()

    @Before
    fun setup() {
        val mockkApplicationInfo = mockk<ApplicationInfo>()
        val mockkPackageName = "packageName"
        every { mockkContext.applicationContext } returns mockkContext
        every { mockkContext.applicationInfo } returns mockkApplicationInfo
        every { mockkContext.packageName } returns mockkPackageName
    }

    @After
    fun end() {
        confirmVerified(mockkContext)
    }

    @Test(expected = NullPointerException::class)
    fun `BuildAppcoinsBilling should throw NullPointerException when Public Key is null`() {
        try {
            CatapultBillingAppCoinsFactory.BuildAppcoinsBilling(mockkContext, null, mockkPurchasesUpdatedListener)
        } catch (ex: NullPointerException) {
            verify(exactly = 3) {
                mockkContext.applicationContext
            }
            verify(exactly = 2) {
                mockkContext.packageName
            }
            verify(exactly = 1) {
                mockkContext.applicationInfo
            }
            throw ex
        }
    }

    @Test
    fun `should create CatapultAppcoinsBilling successfully when values are correct`() {
        val cabResult = CatapultBillingAppCoinsFactory.BuildAppcoinsBilling(
            mockkContext,
            EXAMPLE_BASE_64_ENCODED_KEY,
            mockkPurchasesUpdatedListener
        )

        verify(exactly = 3) {
            mockkContext.applicationContext
        }
        verify(exactly = 2) {
            mockkContext.packageName
        }
        verify(exactly = 1) {
            mockkContext.applicationInfo
        }
        assertTrue(cabResult is CatapultAppcoinsBilling)
    }

    private companion object {
        const val EXAMPLE_BASE_64_ENCODED_KEY = "dGVzdA=="
    }
}
