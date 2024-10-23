package com.appcoins.sdk.core.retrymechanism

import com.appcoins.sdk.core.logger.Logger
import com.appcoins.sdk.core.retrymechanism.exceptions.IncompleteCircularFunctionExecutionException
import com.appcoins.sdk.core.retrymechanism.exceptions.MaxAttemptsReachedException
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RetryHandlerTest {

    @Before
    fun setup() {
        mockkStatic(Logger::class)
    }

    @Test
    fun `should run the function without retrying`() {
        val runningBlock = { true }
        retryUntilSuccess(
            initialInterval = 0,
            exponentialBackoff = false,
            runningBlock = runningBlock,
            onRetryBlock = { }
        )

        verify(exactly = 0) {
            Logger.logInfo(any())
            Logger.logError(any())
        }
    }

    @Test
    fun `should run the function retrying it 3 times`() {
        var counter = 4
        val runningBlock = {
            counter--
            if (counter != 0) {
                throw IncompleteCircularFunctionExecutionException("")
            }
        }
        retryUntilSuccess(
            initialInterval = 0,
            exponentialBackoff = false,
            runningBlock = runningBlock,
            onRetryBlock = { }
        )

        verify(exactly = 3) {
            Logger.logInfo(any())
            Logger.logError(any())
        }
    }

    @Test(expected = MaxAttemptsReachedException::class)
    fun `retryUntilSuccess should throw MaxAttemptsReachedException`() {
        var counter = 4
        val runningBlock = {
            counter--
            if (counter != 0) {
                throw IncompleteCircularFunctionExecutionException("")
            }
        }
        retryUntilSuccess(
            retries = 1,
            initialInterval = 0,
            exponentialBackoff = false,
            runningBlock = runningBlock,
            onRetryBlock = { }
        )
    }
}
