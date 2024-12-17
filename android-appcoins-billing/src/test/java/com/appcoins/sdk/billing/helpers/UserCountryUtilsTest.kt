package com.appcoins.sdk.billing.helpers

import android.content.Context
import android.telephony.TelephonyManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class UserCountryUtilsTest {

    private val mockContext = mockk<Context>()
    private val mockTelephonyManager = mockk<TelephonyManager>()

    @Before
    fun setup() {
        Locale.setDefault(Locale.UK)
        every { mockContext.getSystemService(Context.TELEPHONY_SERVICE) } returns mockTelephonyManager
    }

    @Test
    fun `getUserCountry should return country from SIM`() {
        every { mockTelephonyManager.simCountryIso } returns CORRECTLY_FORMED_COUNTRY

        val result = UserCountryUtils.getUserCountry(mockContext)

        assertEquals(result, CORRECTLY_FORMED_COUNTRY)
    }

    @Test
    fun `getUserCountry should return country from Network`() {
        every { mockTelephonyManager.simCountryIso } returns null
        every { mockTelephonyManager.phoneType } returns TelephonyManager.PHONE_TYPE_GSM
        every { mockTelephonyManager.networkCountryIso } returns CORRECTLY_FORMED_COUNTRY

        val result = UserCountryUtils.getUserCountry(mockContext)

        assertEquals(result, CORRECTLY_FORMED_COUNTRY)
    }

    @Test
    fun `getUserCountry should return country from Locale if phoneType is not acceptable`() {
        every { mockTelephonyManager.simCountryIso } returns null
        every { mockTelephonyManager.phoneType } returns TelephonyManager.PHONE_TYPE_CDMA
        every { mockTelephonyManager.networkCountryIso } returns CORRECTLY_FORMED_COUNTRY

        val result = UserCountryUtils.getUserCountry(mockContext)

        assertEquals(result, Locale.getDefault().country)
    }

    @Test
    fun `getUserCountry should return country from Locale if networkCountryIso is not acceptable`() {
        every { mockTelephonyManager.simCountryIso } returns null
        every { mockTelephonyManager.phoneType } returns TelephonyManager.PHONE_TYPE_GSM
        every { mockTelephonyManager.networkCountryIso } returns null

        val result = UserCountryUtils.getUserCountry(mockContext)

        assertEquals(result, Locale.getDefault().country)
    }

    private companion object {
        const val CORRECTLY_FORMED_COUNTRY = "en"
    }
}
