package com.appcoins.sdk.core.logger

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LoggerTest {

    private val mockkContext = mockk<Context>()

    @Before
    fun setup() {
        mockkStatic(Log::class)
    }

    @Test
    fun `should invoke Info Log`() {
        Logger.logInfo(EXAMPLE_LOG_MESSAGE)

        verify(exactly = 1) { Log.i(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE) }
    }

    @Test
    fun `should invoke Verbose Log`() {
        Logger.logVerbose(EXAMPLE_LOG_MESSAGE)

        verify(exactly = 1) { Log.v(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE) }
    }

    @Test
    fun `should invoke Warning Log`() {
        Logger.logWarning(EXAMPLE_LOG_MESSAGE)

        verify(exactly = 1) { Log.w(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE) }
    }

    @Test
    fun `should invoke Warning Debug Log on Debug build`() {
        val baseApplicationInfo = ApplicationInfo()
        baseApplicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE
        every { mockkContext.applicationContext.applicationInfo } returns baseApplicationInfo
        Logger.setupLogger(mockkContext)
        Logger.logWarningDebug(EXAMPLE_LOG_MESSAGE)

        verify(exactly = 1) { Log.w(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE) }
    }

    @Test
    fun `should not invoke Warning Debug Log on Release build`() {
        val baseApplicationInfo = ApplicationInfo()
        every { mockkContext.applicationContext.applicationInfo } returns baseApplicationInfo
        Logger.setupLogger(mockkContext)
        Logger.logWarningDebug(EXAMPLE_LOG_MESSAGE)

        verify(exactly = 0) { Log.w(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE) }
    }

    @Test
    fun `should invoke Debug Log on Debug build`() {
        val baseApplicationInfo = ApplicationInfo()
        baseApplicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE
        every { mockkContext.applicationContext.applicationInfo } returns baseApplicationInfo
        Logger.setupLogger(mockkContext)
        Logger.logDebug(EXAMPLE_LOG_MESSAGE)

        verify(exactly = 1) { Log.d(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE) }
    }

    @Test
    fun `should not invoke Debug Log on Release build`() {
        val baseApplicationInfo = ApplicationInfo()
        every { mockkContext.applicationContext.applicationInfo } returns baseApplicationInfo
        Logger.setupLogger(mockkContext)
        Logger.logDebug(EXAMPLE_LOG_MESSAGE)

        verify(exactly = 0) { Log.d(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE) }
    }

    @Test
    fun `should invoke Error Log`() {
        Logger.logError(EXAMPLE_LOG_MESSAGE)

        verify(exactly = 1) { Log.e(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE) }
    }

    @Test
    fun `should invoke Verbose Log with Exception`() {
        Logger.logError(EXAMPLE_LOG_MESSAGE, EXAMPLE_LOG_EXCEPTION)

        verify(exactly = 1) { Log.e(any(), BASE_MESSAGE + EXAMPLE_LOG_MESSAGE, EXAMPLE_LOG_EXCEPTION) }
    }

    private companion object {
        const val EXAMPLE_CLASS_NAME = "NativeMethodAccessorImpl"
        const val EXAMPLE_METHOD_NAME = "invoke0"
        const val BASE_MESSAGE = "[$EXAMPLE_CLASS_NAME#$EXAMPLE_METHOD_NAME] "
        const val EXAMPLE_LOG_MESSAGE = "Example log message"
        val EXAMPLE_LOG_EXCEPTION = Exception("Example exception")
    }
}
