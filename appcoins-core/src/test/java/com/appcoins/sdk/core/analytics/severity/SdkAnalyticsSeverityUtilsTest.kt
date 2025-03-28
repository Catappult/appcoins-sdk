package com.appcoins.sdk.core.analytics.severity

import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class SdkAnalyticsSeverityUtilsTest {

    private lateinit var sdkAnalyticsSeverityUtils: SdkAnalyticsSeverityUtils

    @Before
    fun setup() {
        sdkAnalyticsSeverityUtils = SdkAnalyticsSeverityUtils()
    }

    @Test
    fun `should return false for SdkInitializationEvents when no severity levels defined`() {
        SdkAnalyticsUtils.analyticsFlowSeverityLevels = null

        val result = sdkAnalyticsSeverityUtils.isEventSeverityAllowed(SdkInitializationEvents.SdkStartConnection())

        assertFalse(result)
    }

    @Test
    fun `should return false for Sdk when no severity levels defined`() {
        SdkAnalyticsUtils.analyticsFlowSeverityLevels = null

        val result =
            sdkAnalyticsSeverityUtils.isEventSeverityAllowed(SdkPurchaseFlowEvents.SdkLaunchPurchaseMainThreadFailure())

        assertTrue(result)
    }

    @Test
    fun `should return true when severity level of event is acceptable`() {
        val event = SdkInitializationEvents.SdkStartConnection()
        SdkAnalyticsUtils.analyticsFlowSeverityLevels =
            arrayListOf(AnalyticsFlowSeverityLevel(event.flow, event.severityLevel))

        val result = sdkAnalyticsSeverityUtils.isEventSeverityAllowed(event)

        assertTrue(result)
    }

    @Test
    fun `should return false when severity level of event is not acceptable`() {
        val event = SdkInitializationEvents.SdkAttributionRequest()
        SdkAnalyticsUtils.analyticsFlowSeverityLevels =
            arrayListOf(AnalyticsFlowSeverityLevel(event.flow, 1))

        val result = sdkAnalyticsSeverityUtils.isEventSeverityAllowed(event)

        assertFalse(result)
    }

    @Test
    fun `should return false when severity level not found`() {
        val event = SdkInitializationEvents.SdkAttributionRequest()
        SdkAnalyticsUtils.analyticsFlowSeverityLevels = arrayListOf()

        val result = sdkAnalyticsSeverityUtils.isEventSeverityAllowed(event)

        assertFalse(result)
    }
}
