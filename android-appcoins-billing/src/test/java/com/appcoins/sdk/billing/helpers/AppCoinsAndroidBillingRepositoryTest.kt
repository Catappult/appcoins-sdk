package com.appcoins.sdk.billing.helpers

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.sdk.billing.AppcoinsBillingClient.BillingResponseCode
import com.appcoins.sdk.billing.BillingResult
import com.appcoins.sdk.billing.LaunchBillingFlowResult
import com.appcoins.sdk.billing.PurchasesResult
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.SkuDetailsResult
import com.appcoins.sdk.billing.WalletBinderUtil
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener
import com.appcoins.sdk.billing.usecases.RetryFailedRequests
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
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
class AppCoinsAndroidBillingRepositoryTest {

    private lateinit var appCoinsAndroidBillingRepository: AppCoinsAndroidBillingRepository

    private val mockAppcoinsBilling = mockk<AppcoinsBilling>()

    @Before
    fun setup() {
        appCoinsAndroidBillingRepository = AppCoinsAndroidBillingRepository(API_VERSION, PACKAGE_NAME)
    }

    @After
    fun end() {
        confirmVerified()
        unmockkAll()
    }

    @Test
    fun `onConnect should run correctly`() {
        initializeRepository()
    }

    @Test
    fun `onDisconnect should run correctly`() {
        val mockkAppCoinsBillingStateListener = mockk<AppCoinsBillingStateListener>()

        every { mockkAppCoinsBillingStateListener.onBillingServiceDisconnected() } just runs

        appCoinsAndroidBillingRepository.onDisconnect(mockkAppCoinsBillingStateListener)

        verify(exactly = 1) {
            mockkAppCoinsBillingStateListener.onBillingServiceDisconnected()
        }
    }

    @Test(expected = ServiceConnectionException::class)
    fun `getPurchases should throw ServiceConnectionException when service is not ready`() {
        val mockkAppCoinsBillingStateListener = mockk<AppCoinsBillingStateListener>()

        every { mockkAppCoinsBillingStateListener.onBillingServiceDisconnected() } just runs

        // Disconnects the service
        appCoinsAndroidBillingRepository.onDisconnect(mockkAppCoinsBillingStateListener)

        verify(exactly = 1) {
            mockkAppCoinsBillingStateListener.onBillingServiceDisconnected()
        }

        appCoinsAndroidBillingRepository.getPurchases(EMPTY_STRING)
    }

    @Test(expected = ServiceConnectionException::class)
    fun `getPurchases should throw ServiceConnectionException when service throws RemoteException`() {
        initializeRepository()

        every {
            mockAppcoinsBilling.getPurchases(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING,
                null
            )
        } throws RemoteException()

        appCoinsAndroidBillingRepository.getPurchases(EMPTY_STRING)
    }

    @Test
    fun `getPurchases should return correct result when service returns valid value`() {
        initializeRepository()

        mockkStatic(AndroidBillingMapper::class)

        val purchaseResult =
            PurchasesResult(emptyList(), mockBillingResultOk)

        every {
            mockAppcoinsBilling.getPurchases(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING,
                null
            )
        } returns EMPTY_BUNDLE
        every { AndroidBillingMapper.mapPurchases(EMPTY_BUNDLE, EMPTY_STRING) } returns purchaseResult

        val result = appCoinsAndroidBillingRepository.getPurchases(EMPTY_STRING)

        assertEquals(result, purchaseResult)
    }

    @Test(expected = ServiceConnectionException::class)
    fun `querySkuDetailsAsync should throw ServiceConnectionException when service is not ready`() {
        val mockkAppCoinsBillingStateListener = mockk<AppCoinsBillingStateListener>()

        every { mockkAppCoinsBillingStateListener.onBillingServiceDisconnected() } just runs

        // Disconnects the service
        appCoinsAndroidBillingRepository.onDisconnect(mockkAppCoinsBillingStateListener)

        verify(exactly = 1) {
            mockkAppCoinsBillingStateListener.onBillingServiceDisconnected()
        }

        appCoinsAndroidBillingRepository.querySkuDetailsAsync(EMPTY_STRING, EMPTY_STRING_LIST)
    }

    @Test(expected = ServiceConnectionException::class)
    fun `querySkuDetailsAsync should throw ServiceConnectionException when service throws RemoteException`() {
        initializeRepository()

        mockkStatic(AndroidBillingMapper::class)

        every { AndroidBillingMapper.mapArrayListToBundleSkuDetails(EMPTY_STRING_LIST) } returns EMPTY_BUNDLE
        every {
            mockAppcoinsBilling.getSkuDetails(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING,
                EMPTY_BUNDLE
            )
        } throws RemoteException()

        appCoinsAndroidBillingRepository.querySkuDetailsAsync(EMPTY_STRING, EMPTY_STRING_LIST)
    }

    @Test
    fun `querySkuDetailsAsync should return correct result when service returns valid value`() {
        initializeRepository()

        mockkStatic(AndroidBillingMapper::class)
        val skuDetailsResult = SkuDetailsResult(emptyList(), ResponseCode.OK.value)

        every { AndroidBillingMapper.mapArrayListToBundleSkuDetails(EMPTY_STRING_LIST) } returns EMPTY_BUNDLE
        every {
            mockAppcoinsBilling.getSkuDetails(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING,
                EMPTY_BUNDLE
            )
        } returns EMPTY_BUNDLE
        every { AndroidBillingMapper.mapBundleToHashMapSkuDetails(EMPTY_STRING, EMPTY_BUNDLE) } returns skuDetailsResult

        val result = appCoinsAndroidBillingRepository.querySkuDetailsAsync(EMPTY_STRING, EMPTY_STRING_LIST)

        assertEquals(result, skuDetailsResult)
    }

    @Test
    fun `querySkuDetailsAsync should retry until success when service returns SERVICE_UNAVAILABLE response`() {
        initializeRepository()

        mockkStatic(AndroidBillingMapper::class)
        val serviceUnavailableSkuDetailsResult = SkuDetailsResult(emptyList(), ResponseCode.SERVICE_UNAVAILABLE.value)
        val skuDetailsResult = SkuDetailsResult(emptyList(), ResponseCode.OK.value)

        every { AndroidBillingMapper.mapArrayListToBundleSkuDetails(EMPTY_STRING_LIST) } returns EMPTY_BUNDLE
        every {
            mockAppcoinsBilling.getSkuDetails(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING,
                EMPTY_BUNDLE
            )
        } returns EMPTY_BUNDLE
        every {
            AndroidBillingMapper.mapBundleToHashMapSkuDetails(
                EMPTY_STRING,
                EMPTY_BUNDLE
            )
        } returns serviceUnavailableSkuDetailsResult
        Thread {
            Thread.sleep(1000)
            every {
                AndroidBillingMapper.mapBundleToHashMapSkuDetails(
                    EMPTY_STRING,
                    EMPTY_BUNDLE
                )
            } returns skuDetailsResult
        }.start()

        val result = appCoinsAndroidBillingRepository.querySkuDetailsAsync(EMPTY_STRING, EMPTY_STRING_LIST)

        assertEquals(result, skuDetailsResult)
        verify(exactly = 2) {
            mockAppcoinsBilling.getSkuDetails(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING,
                EMPTY_BUNDLE
            )
        }
    }

    @Test(expected = ServiceConnectionException::class)
    fun `consumeAsync should throw ServiceConnectionException when service is not ready`() {
        val mockkAppCoinsBillingStateListener = mockk<AppCoinsBillingStateListener>()

        every { mockkAppCoinsBillingStateListener.onBillingServiceDisconnected() } just runs

        // Disconnects the service
        appCoinsAndroidBillingRepository.onDisconnect(mockkAppCoinsBillingStateListener)

        verify(exactly = 1) {
            mockkAppCoinsBillingStateListener.onBillingServiceDisconnected()
        }

        appCoinsAndroidBillingRepository.consumeAsync(EMPTY_STRING)
    }

    @Test(expected = ServiceConnectionException::class)
    fun `consumeAsync should throw ServiceConnectionException when service throws RemoteException`() {
        initializeRepository()

        every {
            mockAppcoinsBilling.consumePurchase(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING
            )
        } throws RemoteException()

        appCoinsAndroidBillingRepository.consumeAsync(EMPTY_STRING)
    }

    @Test
    fun `consumeAsync should return correct result when service returns valid value`() {
        initializeRepository()

        mockkStatic(AndroidBillingMapper::class)

        every {
            mockAppcoinsBilling.consumePurchase(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING
            )
        } returns ResponseCode.OK.value

        val result = appCoinsAndroidBillingRepository.consumeAsync(EMPTY_STRING)

        assertEquals(result, mockBillingResultOk)
    }

    @Test(expected = ServiceConnectionException::class)
    fun `launchBillingFlow should throw ServiceConnectionException when service is not ready`() {
        val mockkAppCoinsBillingStateListener = mockk<AppCoinsBillingStateListener>()

        every { mockkAppCoinsBillingStateListener.onBillingServiceDisconnected() } just runs

        // Disconnects the service
        appCoinsAndroidBillingRepository.onDisconnect(mockkAppCoinsBillingStateListener)

        verify(exactly = 1) {
            mockkAppCoinsBillingStateListener.onBillingServiceDisconnected()
        }

        appCoinsAndroidBillingRepository.launchBillingFlow(
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING
        )
    }

    @Test(expected = ServiceConnectionException::class)
    fun `launchBillingFlow should throw ServiceConnectionException when service throws RemoteException`() {
        initializeRepository()

        every {
            mockAppcoinsBilling.getBuyIntent(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } throws RemoteException()

        appCoinsAndroidBillingRepository.launchBillingFlow(
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING
        )
    }

    @Test
    fun `launchBillingFlow should return correct result when service returns valid value`() {
        initializeRepository()

        mockkStatic(AndroidBillingMapper::class)

        val launchBillingFlowResult = LaunchBillingFlowResult(ResponseCode.OK.value, Intent())

        every {
            mockAppcoinsBilling.getBuyIntent(
                API_VERSION,
                PACKAGE_NAME,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )
        } returns EMPTY_BUNDLE
        every { AndroidBillingMapper.mapBundleToHashMapGetIntent(EMPTY_BUNDLE) } returns launchBillingFlowResult

        val result =
            appCoinsAndroidBillingRepository.launchBillingFlow(
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING
            )

        assertEquals(result, launchBillingFlowResult)
    }

    private fun initializeRepository() {
        mockkStatic(WalletBinderUtil::class)
        mockkStatic(AppcoinsBillingStubHelper.Stub::class)
        mockkObject(RetryFailedRequests)
        mockkStatic(BillingResult.Builder::class)
        mockkObject(BillingResult)

        val mockkComponentName = mockk<ComponentName>()
        val mockkIBinder = mockk<IBinder>()
        val mockkAppCoinsBillingStateListener = mockk<AppCoinsBillingStateListener>()

        every { mockkComponentName.className } returns "className"
        every { RetryFailedRequests.invoke() } just runs
        every { WalletBinderUtil.bindType } returns BindType.BILLING_SERVICE_NOT_INSTALLED
        every { AppcoinsBillingStubHelper.Stub.asInterface(mockkIBinder) } returns mockAppcoinsBilling
        every { mockkAppCoinsBillingStateListener.onBillingSetupFinished(mockBillingResultOk) } just runs
        every { BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build() } returns mockBillingResultOk

        appCoinsAndroidBillingRepository.onConnect(
            mockkComponentName,
            mockkIBinder,
            mockkAppCoinsBillingStateListener
        )

        verify(exactly = 1) {
            RetryFailedRequests.invoke()
            mockkAppCoinsBillingStateListener.onBillingSetupFinished(mockBillingResultOk)
        }
    }

    private companion object {
        const val API_VERSION = 3
        const val PACKAGE_NAME = "packageName"

        const val EMPTY_STRING = ""
        val EMPTY_STRING_LIST = emptyList<String>()
        val EMPTY_BUNDLE = Bundle()

        val mockBillingResultOk = BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
    }
}
