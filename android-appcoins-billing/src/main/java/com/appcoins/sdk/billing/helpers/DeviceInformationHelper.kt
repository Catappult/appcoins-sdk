package com.appcoins.sdk.billing.helpers

import android.os.Build
import java.util.Locale

fun getDeviceInfo(): DeviceInformation {
    return DeviceInformation(
        osVersion = Build.VERSION.RELEASE,
        brand = Build.BRAND,
        model = Build.MODEL,
        language = Locale.getDefault().language,
        isProbablyEmulator = isProbablyEmulator()
    )
}

class DeviceInformation(
    val osVersion: String,
    val brand: String,
    val model: String,
    val language: String,
    val isProbablyEmulator: Boolean
)

@Suppress("complexity:CyclomaticComplexMethod")
private fun isProbablyEmulator(): Boolean {
    return (
        (
            Build.FINGERPRINT.startsWith("google/sdk_gphone_") &&
                Build.FINGERPRINT.endsWith(":user/release-keys") &&
                Build.MANUFACTURER == "Google" &&
                Build.PRODUCT.startsWith("sdk_gphone_") &&
                Build.BRAND == "google" &&
                Build.MODEL.startsWith("sdk_gphone_")
            ) || Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            /* Bluestacks */
            (
                "QC_Reference_Phone" == Build.BOARD &&
                    !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
                ) ||
            Build.MANUFACTURER.contains("Genymotion") ||
            /* MSI App Player */
            Build.HOST.startsWith("Build") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            Build.PRODUCT == "google_sdk"
        )
}
