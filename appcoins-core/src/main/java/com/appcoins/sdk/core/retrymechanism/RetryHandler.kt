package com.appcoins.sdk.core.retrymechanism

import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

fun <T> retryUntilSuccess(
    retries: Int? = null,
    initialInterval: Long = 1000,
    exponentialBackoff: Boolean = false,
    maxInterval: Long = Long.MAX_VALUE,
    block: () -> T
): T? {
    var retriesLeft = retries
    var interval = initialInterval
    var attempt = 0

    while (retriesLeft?.let { it > 0 } != false) {
        try {
            return block()
        } catch (e: IncompleteCircularFunctionExecutionException) {
            attempt++
            if (retriesLeft != null) {
                retriesLeft--
            }

            logError("Attempt $attempt failed: ${e.message}")
            logInfo("Retrying in $interval milliseconds...")

            Thread.sleep(interval)

            if (exponentialBackoff) {
                interval = (interval * 2).coerceAtMost(maxInterval)
            }
        }
    }

    throw Exception("Max retries exhausted")
}
