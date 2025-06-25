package com.appcoins.sdk.core.analytics

import com.appcoins.sdk.core.analytics.indicative.IndicativeEventLogger
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.indicative.client.android.Indicative
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class IndicativeEventLoggerTest {

    private lateinit var indicativeEventLogger: IndicativeEventLogger

    @Before
    fun setup() {
        indicativeEventLogger = IndicativeEventLogger
    }

    @Test
    fun `should log event when logEvent is called`() {
        mockkStatic(Indicative::class)

        val eventName = "exampleEventName"

        every { Indicative.recordEvent(eventName, any(), any<MutableMap<String, Any>>()) } just runs

        indicativeEventLogger.logEvent(
            eventName,
            null,
            AnalyticsManager.Action.IMPRESSION,
            EVENT_CONTEXT
        )

        verify(exactly = 1) {
            Indicative.recordEvent(eventName, any(), any<MutableMap<String, Any>>())
        }
    }

    private companion object {
        const val EVENT_CONTEXT = "AnalyticsSDK"
    }
}
