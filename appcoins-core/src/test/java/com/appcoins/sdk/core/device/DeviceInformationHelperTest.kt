package com.appcoins.sdk.core.device

import android.os.Build
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
 Robolectric sets mockk values for the Locale and Build classes.
 */
@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class DeviceInformationHelperTest {

    @Test
    fun `getDeviceInfo should return Device as emulator`() {
        ReflectionHelpers.setStaticField(Build::class.java, "FINGERPRINT", "generic")
        val deviceResult = getDeviceInfo()

        assertTrue(deviceResult.isProbablyEmulator)
    }

    @Test
    fun `getDeviceInfo should return normal Device`() {
        val deviceResult = getDeviceInfo()

        assertFalse(deviceResult.isProbablyEmulator)
    }
}
