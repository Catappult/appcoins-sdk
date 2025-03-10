package com.appcoins.sdk.core.network.retrymechanism

import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.network.retrymechanism.exceptions.IncompleteCircularFunctionExecutionException
import com.appcoins.sdk.core.network.retrymechanism.exceptions.MaxAttemptsReachedException

fun <T> retryUntilSuccess(
    retries: Int? = null,
    initialInterval: Long = 1000,
    exponentialBackoff: Boolean = false,
    maxInterval: Long = Long.MAX_VALUE,
    runningBlock: () -> T,
    onRetryBlock: (attempts: Int) -> Unit
): T? {
    var retriesLeft = retries
    var interval = initialInterval
    var attempt = 0

    while (retriesLeft?.let { it > 0 } != false) {
        try {
            return runningBlock()
        } catch (e: IncompleteCircularFunctionExecutionException) {
            attempt++
            if (retriesLeft != null) {
                retriesLeft--
            }

            logError("Attempt $attempt failed: ${e.message}")
            logInfo("Retrying in $interval milliseconds...")

            onRetryBlock(attempt)

            Thread.sleep(interval)

            if (exponentialBackoff) {
                interval = (interval * 2).coerceAtMost(maxInterval)
            }
        }
    }

    throw MaxAttemptsReachedException()
}
