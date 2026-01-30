package com.appcoins.sdk.core.date
import java.text.SimpleDateFormat
import java.util.Locale

fun parseIsoToMillis(isoString: String?): Long {
    if (isoString.isNullOrEmpty()) return 0L

    return try {
        val normalizedDate = isoString.replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        sdf.parse(normalizedDate)?.time ?: 0L
    } catch (_: Exception) {
        0L
    }
}
