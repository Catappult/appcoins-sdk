package com.appcoins.sdk.billing.helpers

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.managers.ApiKeysManager
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.indicative.client.android.Indicative
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.util.ReflectionHelpers
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class WalletUtilsTest {

    private val mockkContext = mockk<Context>()

    @Before
    fun setup() {
        mockkObject(WalletUtils)

        every { mockkContext.packageName } returns EMPTY_STRING

        WalletUtils.context = mockkContext
    }

    @After
    fun end() {
        unmockkAll()
    }

    @Test
    fun `startWebFirstPayment should return BILLING_UNAVAILABLE when running in MainTread`() {
        mockkStatic(Looper::class)
        val mockkLooper = mockk<Looper>()

        every { Looper.getMainLooper() } returns mockkLooper
        every { Looper.myLooper() } returns mockkLooper

        val result = WalletUtils.startWebFirstPayment(EMPTY_STRING, EMPTY_STRING, null)

        assertEquals(result.getInt(RESPONSE_CODE), ResponseCode.BILLING_UNAVAILABLE.value)
    }

    @Test
    fun `startWebFirstPayment should return ERROR when webPaymentUrl is null`() {
        mockkStatic(Looper::class)
        val mockkLooper = mockk<Looper>()
        val mockkMyLooper = mockk<Looper>()

        every { Looper.getMainLooper() } returns mockkLooper
        every { Looper.myLooper() } returns mockkMyLooper

        WalletUtils.webPaymentUrl = null

        val result = WalletUtils.startWebFirstPayment(EMPTY_STRING, EMPTY_STRING, null)

        assertEquals(ResponseCode.ERROR.value, result.getInt(RESPONSE_CODE))
    }

    @Test
    fun `startWebFirstPayment should return OK when generates correctly Bundle`() {
        mockkStatic(Looper::class)
        val mockkLooper = mockk<Looper>()
        val mockkMyLooper = mockk<Looper>()

        every { Looper.getMainLooper() } returns mockkLooper
        every { Looper.myLooper() } returns mockkMyLooper

        WalletUtils.webPaymentUrl = EMPTY_STRING

        val result = WalletUtils.startWebFirstPayment(EMPTY_STRING, EMPTY_STRING, null)

        assertEquals(ResponseCode.OK.value, result.getInt(RESPONSE_CODE))
    }

    @Test
    fun `startWalletPayment should return OK when generates correctly Bundle`() {
        val result = WalletUtils.startWalletPayment(EMPTY_BUNDLE, EMPTY_STRING)

        assertEquals(ResponseCode.OK.value, result.getInt(RESPONSE_CODE))
    }

    @Test
    fun `startInstallFlow should return BILLING_UNAVAILABLE when Wallet not available in the Device VERSION`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 20)

        val result = WalletUtils.startInstallFlow(null)

        assertEquals(ResponseCode.BILLING_UNAVAILABLE.value, result.getInt(RESPONSE_CODE))
    }

    @Test
    fun `startInstallFlow should return OK when generates correctly Bundle`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 21)

        val result = WalletUtils.startInstallFlow(null)

        assertEquals(ResponseCode.OK.value, result.getInt(RESPONSE_CODE))
    }

    @Test
    fun `startIndicative should return OK when generates correctly Bundle`() {
        mockkStatic(Indicative::class)
        mockkObject(ApiKeysManager)

        val mockkIndicative = mockk<Indicative>()
        val mockkSharedPreferences = mockk<SharedPreferences>()

        every { Indicative.launch(mockkContext, EMPTY_STRING) } returns mockkIndicative
        every { ApiKeysManager.getIndicativeApiKey() } returns EMPTY_STRING
        every { PreferenceManager.getDefaultSharedPreferences(mockkContext) } returns mockkSharedPreferences
        every { mockkSharedPreferences.getString(WALLET_ID_KEY, null) } returns EMPTY_STRING
        every { WalletUtils.sdkAnalytics.sendStartConnectionEvent() } just runs

        WalletUtils.startIndicative(EMPTY_STRING)

        shadowOf(Looper.getMainLooper()).idle()
        shadowOf(Looper.getMainLooper()).runToNextTask()

        verify(exactly = 1) { WalletUtils.sdkAnalytics.sendStartConnectionEvent() }
    }

    private companion object {
        const val EMPTY_STRING = ""
        val EMPTY_BUNDLE = Bundle()
        const val WALLET_ID_KEY = "WALLET_ID"
    }
}
