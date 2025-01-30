package com.appcoins.sdk.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
class NetworkTrafficTest {

    private val networkTraffic: NetworkTraffic = NetworkTraffic()

    @Test
    fun `should return null if an Exception was thrown`() {
        val mockkContext = mockk<Context>()

        every { mockkContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns null

        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.M)

        val result = networkTraffic.getAverageSpeed(mockkContext)

        assertNull(result)
    }

    @Test
    fun `should return null if Build VERSION is not above KITKAT_WATCH`() {
        val mockkContext = mockk<Context>()
        every { mockkContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns null

        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.KITKAT_WATCH)

        val result = networkTraffic.getAverageSpeed(mockkContext)

        assertNull(result)
    }

    @Test
    fun `should return 0 if Build VERSION is LOLLIPOP and no internet connection available`() {
        val mockkContext = mockk<Context>()
        val mockkConnectivityManager = mockk<ConnectivityManager>()
        val mockkActiveNetworkInfo = mockk<NetworkInfo>()

        every { mockkContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockkConnectivityManager
        every { mockkConnectivityManager.activeNetworkInfo } returns mockkActiveNetworkInfo
        every { mockkActiveNetworkInfo.isConnected } returns false

        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.LOLLIPOP)

        val result = networkTraffic.getAverageSpeed(mockkContext)

        assertEquals(result, "0")
    }

    @Test
    fun `should return 0 if Build VERSION is LOLLIPOP and no active networks info found`() {
        val mockkContext = mockk<Context>()
        val mockkConnectivityManager = mockk<ConnectivityManager>()

        every { mockkContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockkConnectivityManager
        every { mockkConnectivityManager.activeNetworkInfo } returns null

        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.LOLLIPOP)

        val result = networkTraffic.getAverageSpeed(mockkContext)

        assertEquals(result, "0")
    }

    @Test
    fun `should return -1 if Build VERSION is LOLLIPOP and internet connection is available`() {
        val mockkContext = mockk<Context>()
        val mockkConnectivityManager = mockk<ConnectivityManager>()
        val mockkActiveNetworkInfo = mockk<NetworkInfo>()

        every { mockkContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockkConnectivityManager
        every { mockkConnectivityManager.activeNetworkInfo } returns mockkActiveNetworkInfo
        every { mockkActiveNetworkInfo.isConnected } returns true

        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.LOLLIPOP)

        val result = networkTraffic.getAverageSpeed(mockkContext)

        assertEquals(result, "-1")
    }

    @Test
    fun `should return correct network speed if Build VERSION is M`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", Build.VERSION_CODES.M)

        val mockkContext = mockk<Context>()
        val mockkConnectivityManager = mockk<ConnectivityManager>()
        val mockkActiveNetwork = mockk<Network>()
        val mockkNetworkCapabilities = mockk<NetworkCapabilities>()

        every { mockkContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockkConnectivityManager
        every { mockkConnectivityManager.activeNetwork } returns mockkActiveNetwork
        every { mockkConnectivityManager.getNetworkCapabilities(mockkActiveNetwork) } returns mockkNetworkCapabilities
        every { mockkNetworkCapabilities.linkDownstreamBandwidthKbps } returns 1000

        val result = networkTraffic.getAverageSpeed(mockkContext)

        assertEquals(result, "1")
    }
}
