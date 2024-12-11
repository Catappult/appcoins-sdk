package com.appcoins.sdk.billing.helpers

import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.managers.ProductV2Manager
import com.appcoins.sdk.billing.mappers.PurchasesResponse
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class AppcoinsBillingWrapperTest {

    private lateinit var appcoinsBillingWrapper: AppcoinsBillingWrapper
    private lateinit var appcoinsBillingWrapperWithoutWalletId: AppcoinsBillingWrapper

    private val mockkAppcoinsBilling = mockk<AppcoinsBilling>()

    @Before
    fun setup() {
        appcoinsBillingWrapper = AppcoinsBillingWrapper(mockkAppcoinsBilling, WALLET_ID)
        appcoinsBillingWrapperWithoutWalletId = AppcoinsBillingWrapper(mockkAppcoinsBilling, null)
    }

    @After
    fun end() {
        confirmVerified(mockkAppcoinsBilling)
        unmockkAll()
    }

    @Test
    fun `isBillingSupported should return correct value when AppcoinsBilling returns valid value`() {
        every { mockkAppcoinsBilling.isBillingSupported(API_VERSION, EMPTY_STRING, EMPTY_STRING) } returns 0

        val result = appcoinsBillingWrapper.isBillingSupported(API_VERSION, EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 2) {
            mockkAppcoinsBilling.isBillingSupported(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        }
        assertEquals(result, 0)
    }

    @Test(expected = RemoteException::class)
    fun `isBillingSupported should throw RemoteException when AppcoinsBilling is not available`() {
        every {
            mockkAppcoinsBilling.isBillingSupported(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } throws RemoteException()

        try {
            appcoinsBillingWrapper.isBillingSupported(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        } catch (ex: RemoteException) {
            verify(exactly = 2) {
                mockkAppcoinsBilling.isBillingSupported(API_VERSION, EMPTY_STRING, EMPTY_STRING)
            }
            throw ex
        }
    }

    @Test
    fun `getSkuDetails should return correct value when AppcoinsBilling returns valid value`() {
        every {
            mockkAppcoinsBilling.getSkuDetails(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_BUNDLE
            )
        } returns EMPTY_BUNDLE

        val result = appcoinsBillingWrapper.getSkuDetails(API_VERSION, EMPTY_STRING, EMPTY_STRING, EMPTY_BUNDLE)

        verify(exactly = 1) {
            mockkAppcoinsBilling.getSkuDetails(API_VERSION, EMPTY_STRING, EMPTY_STRING, EMPTY_BUNDLE)
        }
        assertEquals(result, EMPTY_BUNDLE)
    }

    @Test(expected = RemoteException::class)
    fun `getSkuDetails should throw RemoteException when AppcoinsBilling is not available`() {
        every {
            mockkAppcoinsBilling.getSkuDetails(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_BUNDLE
            )
        } throws RemoteException()

        try {
            appcoinsBillingWrapper.getSkuDetails(API_VERSION, EMPTY_STRING, EMPTY_STRING, EMPTY_BUNDLE)
        } catch (ex: RemoteException) {
            verify(exactly = 1) {
                mockkAppcoinsBilling.getSkuDetails(API_VERSION, EMPTY_STRING, EMPTY_STRING, EMPTY_BUNDLE)
            }
            throw ex
        }
    }

    @Test
    fun `getBuyIntent should return correct value when AppcoinsBilling returns valid value`() {
        mockkObject(WalletUtils)
        every {
            mockkAppcoinsBilling.getBuyIntent(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } returns EMPTY_BUNDLE
        every {
            WalletUtils.startWalletPayment(EMPTY_BUNDLE, EMPTY_STRING)
        } returns EMPTY_BUNDLE

        val result = appcoinsBillingWrapper.getBuyIntent(
            API_VERSION,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING
        )

        verify(exactly = 1) {
            mockkAppcoinsBilling.getBuyIntent(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
            WalletUtils.startWalletPayment(EMPTY_BUNDLE, EMPTY_STRING)
        }
        assertEquals(result, EMPTY_BUNDLE)
    }

    @Test(expected = RemoteException::class)
    fun `getBuyIntent should throw RemoteException when AppcoinsBilling is not available`() {
        mockkObject(WalletUtils)
        every {
            mockkAppcoinsBilling.getBuyIntent(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } throws RemoteException()

        try {
            appcoinsBillingWrapper.getBuyIntent(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } catch (ex: RemoteException) {
            verify(exactly = 1) {
                mockkAppcoinsBilling.getBuyIntent(
                    API_VERSION,
                    EMPTY_STRING,
                    EMPTY_STRING,
                    EMPTY_STRING,
                    EMPTY_STRING,
                    EMPTY_STRING,
                    EMPTY_STRING
                )
            }
            throw ex
        }
    }

    @Test
    fun `getPurchases without WalletID should return correct value when AppcoinsBilling returns valid value`() {
        every {
            mockkAppcoinsBilling.getPurchases(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } returns EMPTY_BUNDLE

        val result = appcoinsBillingWrapperWithoutWalletId.getPurchases(
            API_VERSION,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING
        )

        verify(exactly = 1) {
            mockkAppcoinsBilling.getPurchases(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        }
        assertEquals(result, EMPTY_BUNDLE)
    }

    @Test
    fun `getPurchases with WalletID should return correct value when AppcoinsBilling returns valid value`() {
        mockkObject(ProductV2Manager)

        val purchaseResponse = PurchasesResponse(ResponseCode.OK.value, emptyList())

        every {
            mockkAppcoinsBilling.getPurchases(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } returns EMPTY_BUNDLE
        every { ProductV2Manager.getPurchasesSync(EMPTY_STRING, WALLET_ID, EMPTY_STRING) } returns purchaseResponse

        val result = appcoinsBillingWrapper.getPurchases(
            API_VERSION,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING
        )

        verify(exactly = 1) {
            mockkAppcoinsBilling.getPurchases(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
            ProductV2Manager.getPurchasesSync(EMPTY_STRING, WALLET_ID, EMPTY_STRING)
        }
        assertEquals(result, EMPTY_BUNDLE)
    }

    @Test(expected = RemoteException::class)
    fun `getPurchases should throw RemoteException when AppcoinsBilling is not available`() {
        every {
            mockkAppcoinsBilling.getPurchases(
                API_VERSION,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } throws RemoteException()

        try {
            appcoinsBillingWrapper.getPurchases(API_VERSION, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING)
        } catch (ex: RemoteException) {
            verify(exactly = 1) {
                mockkAppcoinsBilling.getPurchases(API_VERSION, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING)
            }
            throw ex
        }
    }

    @Test
    fun `asBinder should return BINDER from AppcoinsBilling`() {
        val mockkBinder = mockk<IBinder>()
        every { mockkAppcoinsBilling.asBinder() } returns mockkBinder

        val result = appcoinsBillingWrapper.asBinder()

        verify(exactly = 1) {
            mockkAppcoinsBilling.asBinder()
        }
        assertEquals(result, mockkBinder)
    }

    @Test
    fun `consumePurchase should return correct value when AppcoinsBilling returns valid value`() {
        every {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        } returns ResponseCode.OK.value

        val result = appcoinsBillingWrapper.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        }
        assertEquals(result, ResponseCode.OK.value)
    }

    @Test(expected = RemoteException::class)
    fun `consumePurchase should throw RemoteException when AppcoinsBilling is not available`() {
        every {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        } throws RemoteException()

        try {
            appcoinsBillingWrapper.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        } catch (ex: RemoteException) {
            verify(exactly = 1) {
                mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
            }
            throw ex
        }
    }

    @Test
    fun `consumePurchase should return ERROR when AppcoinsBilling returns invalid value and WalletID is not present`() {
        mockkObject(ProductV2Manager)

        every {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        } returns ResponseCode.ERROR.value

        val result = appcoinsBillingWrapperWithoutWalletId.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        }
        assertEquals(result, ResponseCode.ERROR.value)
    }

    @Test
    fun `consumePurchase should return ERROR when AppcoinsBilling returns invalid value and API_VERSION is invalid`() {
        mockkObject(ProductV2Manager)

        every {
            mockkAppcoinsBilling.consumePurchase(INCORRECT_API_VERSION, EMPTY_STRING, EMPTY_STRING)
        } returns ResponseCode.ERROR.value

        val result = appcoinsBillingWrapper.consumePurchase(INCORRECT_API_VERSION, EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAppcoinsBilling.consumePurchase(INCORRECT_API_VERSION, EMPTY_STRING, EMPTY_STRING)
        }
        assertEquals(result, ResponseCode.ERROR.value)
    }

    @Test
    fun `consumePurchase should return correct value when AppcoinsBilling returns invalid value but ProductV2Manager consumed correctly`() {
        mockkObject(ProductV2Manager)

        every {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        } returns ResponseCode.ERROR.value
        every {
            ProductV2Manager.consumePurchase(WALLET_ID, EMPTY_STRING, EMPTY_STRING)
        } returns ResponseCode.OK.value

        val result = appcoinsBillingWrapper.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
            ProductV2Manager.consumePurchase(WALLET_ID, EMPTY_STRING, EMPTY_STRING)
        }
        assertEquals(result, ResponseCode.OK.value)
    }

    @Test
    fun `consumePurchase should return ERROR when AppcoinsBilling and ProductV2Manager return invalid value`() {
        mockkObject(ProductV2Manager)

        every {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
        } returns ResponseCode.ERROR.value
        every {
            ProductV2Manager.consumePurchase(WALLET_ID, EMPTY_STRING, EMPTY_STRING)
        } returns ResponseCode.ERROR.value

        val result = appcoinsBillingWrapper.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAppcoinsBilling.consumePurchase(API_VERSION, EMPTY_STRING, EMPTY_STRING)
            ProductV2Manager.consumePurchase(WALLET_ID, EMPTY_STRING, EMPTY_STRING)
        }
        assertEquals(result, ResponseCode.ERROR.value)
    }

    private companion object {
        const val WALLET_ID = "walletId"

        const val API_VERSION = 3
        const val INCORRECT_API_VERSION = 1
        const val EMPTY_STRING = ""

        val EMPTY_BUNDLE = Bundle()
    }
}
