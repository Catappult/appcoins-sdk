package com.appcoins.sdk.core.analytics

import android.content.Context
import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableEvents
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseEvents
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureEvents
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureStep
import com.appcoins.sdk.core.analytics.events.SdkGetReferralDeeplinkEvents
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogEvents
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateDialogEvents
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateEvents
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateStoreEvents
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesEvents
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsEvents
import com.appcoins.sdk.core.analytics.events.SdkWalletPaymentFlowEvents
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Suppress("MaxLineLength")
class SdkAnalyticsTest {

    private lateinit var sdkAnalytics: SdkAnalytics

    private val mockkAnalyticsManager = mockk<AnalyticsManager>()

    @Before
    fun setup() {
        sdkAnalytics = SdkAnalytics(mockkAnalyticsManager)
    }

    @Test
    fun `sendAppUpdateAvailableRequest should send event`() {
        val analyticsEvent = SdkAppUpdateAvailableEvents.SdkAppUpdateAvailableRequest()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAppUpdateAvailableRequest()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAppUpdateAvailableResult should send event`() {
        val analyticsEvent = SdkAppUpdateAvailableEvents.SdkAppUpdateAvailableResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAppUpdateAvailableResult(DEFAULT_BOOLEAN)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAppUpdateAvailableMainThreadFailure should send event`() {
        val analyticsEvent = SdkAppUpdateAvailableEvents.SdkAppUpdateAvailableMainThreadFailure()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAppUpdateAvailableMainThreadFailure()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAppUpdateAvailableFailureToObtainResult should send event`() {
        val analyticsEvent = SdkAppUpdateAvailableEvents.SdkAppUpdateAvailableFailureToObtainResult()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAppUpdateAvailableFailureToObtainResult()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendBackendRequestEvent should send event`() {
        val analyticsEvent = SdkBackendRequestEvents.SdkCallBackendRequest(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendBackendRequestEvent(
            SdkBackendRequestType.APP_VERSION,
            EMPTY_STRING,
            EMPTY_STRING,
            mutableListOf(),
            mutableMapOf(),
            mutableMapOf(),
            mutableMapOf()
        )

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendBackendResponseEvent should send event`() {
        val analyticsEvent = SdkBackendRequestEvents.SdkCallBackendResponse(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendBackendResponseEvent(
            SdkBackendRequestType.APP_VERSION,
            DEFAULT_INTEGER,
            EMPTY_STRING,
        )

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendBackendErrorEvent should send event`() {
        val mockkContext = mockk<Context>()
        val analyticsEvent = SdkBackendRequestEvents.SdkCallBackendError(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendBackendErrorEvent(
            SdkBackendRequestType.APP_VERSION,
            EMPTY_STRING,
            EMPTY_STRING,
            mockkContext,
        )

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendBackendMappingFailureEvent should send event`() {
        val analyticsEvent = SdkBackendRequestEvents.SdkCallBackendMappingFailure(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendBackendMappingFailureEvent(
            SdkBackendRequestType.APP_VERSION,
            EMPTY_STRING,
            EMPTY_STRING,
        )

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendConsumePurchaseRequest should send event`() {
        val analyticsEvent = SdkConsumePurchaseEvents.SdkConsumePurchaseRequest(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendConsumePurchaseRequest(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendConsumePurchaseResult should send event`() {
        val analyticsEvent = SdkConsumePurchaseEvents.SdkConsumePurchaseResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendConsumePurchaseResult(EMPTY_STRING, DEFAULT_INTEGER)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendServiceConnectionExceptionEvent should send event`() {
        val analyticsEvent = SdkGeneralFailureEvents.SdkServiceConnectionException(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendServiceConnectionExceptionEvent(SdkGeneralFailureStep.CONSUME)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendPurchaseSignatureVerificationFailureEvent should send event`() {
        val analyticsEvent = SdkGeneralFailureEvents.SdkPurchaseSignatureVerificationFailure(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendPurchaseSignatureVerificationFailureEvent(EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendUnexpectedFailureEvent should send event`() {
        val analyticsEvent = SdkGeneralFailureEvents.SdkUnexpectedFailure(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendUnexpectedFailureEvent(EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendGetReferralDeeplinkRequestEvent should send event`() {
        val analyticsEvent = SdkGetReferralDeeplinkEvents.SdkGetReferralDeeplinkRequest()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendGetReferralDeeplinkRequestEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendGetReferralDeeplinkResultEvent should send event`() {
        val analyticsEvent = SdkGetReferralDeeplinkEvents.SdkGetReferralDeeplinkResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendGetReferralDeeplinkResultEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendGetReferralDeeplinkMainThreadFailureEvent should send event`() {
        val analyticsEvent = SdkGetReferralDeeplinkEvents.SdkGetReferralDeeplinkMainThreadFailure()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendGetReferralDeeplinkMainThreadFailureEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendStartConnectionEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkStartConnection()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendStartConnectionEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendServiceConnectedEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkServiceConnected(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendServiceConnectedEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendServiceConnectionFailureEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkServiceConnectionFailure(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendServiceConnectionFailureEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendFinishConnectionEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkFinishConnection()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendFinishConnectionEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAttributionRequestEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkAttributionRequest()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAttributionRequestEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAttributionResultEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkAttributionResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAttributionResultEvent(
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
        )

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAttributionRetryAttemptEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkAttributionRetryAttempt(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAttributionRetryAttemptEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAttributionRequestFailureEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkAttributionRequestFailure()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAttributionRequestFailureEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendPayflowRequestEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkPayflowRequest()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendPayflowRequestEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendPayflowResultEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkPayflowResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendPayflowResultEvent(arrayListOf())

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAppInstallationTriggerEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkAppInstallationTrigger(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAppInstallationTriggerEvent(EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendInstallWalletDialogEvent should send event`() {
        val analyticsEvent = SdkInstallWalletDialogEvents.SdkInstallWalletDialog()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendInstallWalletDialogEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendInstallWalletDialogActionEvent should send event`() {
        val analyticsEvent = SdkInstallWalletDialogEvents.SdkInstallWalletDialogAction(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendInstallWalletDialogActionEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendInstallWalletDialogDownloadWalletVanillaEvent should send event`() {
        val analyticsEvent = SdkInstallWalletDialogEvents.SdkInstallWalletDialogDownloadWalletVanilla()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendInstallWalletDialogDownloadWalletVanillaEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendInstallWalletDialogDownloadWalletFallbackEvent should send event`() {
        val analyticsEvent = SdkInstallWalletDialogEvents.SdkInstallWalletDialogDownloadWalletFallback(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendInstallWalletDialogDownloadWalletFallbackEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendInstallWalletDialogSuccessEvent should send event`() {
        val analyticsEvent = SdkInstallWalletDialogEvents.SdkInstallWalletDialogSuccess()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendInstallWalletDialogSuccessEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendLaunchAppUpdateResultEvent should send event`() {
        val analyticsEvent = SdkLaunchAppUpdateEvents.SdkLaunchAppUpdateResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchAppUpdateResultEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendLaunchAppUpdateDeeplinkFailureEvent should send event`() {
        val analyticsEvent = SdkLaunchAppUpdateEvents.SdkLaunchAppUpdateDeeplinkFailure(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchAppUpdateDeeplinkFailureEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendLaunchAppUpdateDialogRequestEvent should send event`() {
        val analyticsEvent = SdkLaunchAppUpdateDialogEvents.SdkLaunchAppUpdateDialogRequest()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchAppUpdateDialogRequestEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendLaunchAppUpdateDialogActionEvent should send event`() {
        val analyticsEvent = SdkLaunchAppUpdateDialogEvents.SdkLaunchAppUpdateDialogAction(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchAppUpdateDialogActionEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendLaunchAppUpdateStoreRequestEvent should send event`() {
        val analyticsEvent = SdkLaunchAppUpdateStoreEvents.SdkLaunchAppUpdateStoreRequest()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchAppUpdateStoreRequestEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendLaunchPurchaseEvent should send event`() {
        val analyticsEvent = SdkPurchaseFlowEvents.SdkLaunchPurchase(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchPurchaseEvent(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendPurchaseResultEvent should send event`() {
        val analyticsEvent = SdkPurchaseFlowEvents.SdkPurchaseResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendPurchaseResultEvent(DEFAULT_INTEGER, EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendLaunchPurchaseTypeNotSupportedFailureEvent should send event`() {
        val analyticsEvent = SdkPurchaseFlowEvents.SdkLaunchPurchaseTypeNotSupportedFailure(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchPurchaseTypeNotSupportedFailureEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendLaunchPurchaseMainThreadFailureEvent should send event`() {
        val analyticsEvent = SdkPurchaseFlowEvents.SdkLaunchPurchaseMainThreadFailure()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchPurchaseMainThreadFailureEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentStartEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentStart(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentStartEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentFailureToObtainUrlEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentFailureToObtainUrl()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentFailureToObtainUrlEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentFailureToOpenDeeplinkEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentFailureToOpenDeeplink(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentFailureToOpenDeeplinkEvent(EMPTY_STRING, EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentErrorProcessingPurchaseResultEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentErrorProcessingPurchaseResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentErrorProcessingPurchaseResultEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentPurchaseResultEmptyEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentPurchaseResultEmpty()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentPurchaseResultEmptyEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentOpenDeeplinkEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentOpenDeeplink(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentOpenDeeplinkEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentLaunchExternalPaymentEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentLaunchExternalPayment(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentLaunchExternalPaymentEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentAllowExternalAppsEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentAllowExternalApps(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentAllowExternalAppsEvent(DEFAULT_BOOLEAN)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentExternalPaymentResultEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentExternalPaymentResult()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentExternalPaymentResultEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentExecuteExternalDeeplinkEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentExecuteExternalDeeplink(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentExecuteExternalDeeplinkEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentWalletPaymentResultEvent should send event`() {
        val analyticsEvent = SdkWebPaymentFlowEvents.SdkWebPaymentWalletPaymentResult()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentWalletPaymentResultEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWalletPaymentStartEvent should send event`() {
        val analyticsEvent = SdkWalletPaymentFlowEvents.SdkWalletPaymentStart()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWalletPaymentStartEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWalletPaymentEmptyDataEvent should send event`() {
        val analyticsEvent = SdkWalletPaymentFlowEvents.SdkWalletPaymentEmptyData()
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWalletPaymentEmptyDataEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendQueryPurchasesRequestEvent should send event`() {
        val analyticsEvent = SdkQueryPurchasesEvents.SdkQueryPurchasesRequest(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendQueryPurchasesRequestEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendQueryPurchasesTypeNotSupportedErrorEvent should send event`() {
        val analyticsEvent = SdkQueryPurchasesEvents.SdkQueryPurchasesTypeNotSupportedError(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendQueryPurchasesTypeNotSupportedErrorEvent(EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendQueryPurchasesResultEvent should send event`() {
        val analyticsEvent = SdkQueryPurchasesEvents.SdkQueryPurchasesResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendQueryPurchasesResultEvent(arrayListOf())

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendQuerySkuDetailsRequestEvent should send event`() {
        val analyticsEvent = SdkQuerySkuDetailsEvents.SdkQuerySkuDetailsRequest(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendQuerySkuDetailsRequestEvent(arrayListOf(), EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendQuerySkuDetailsResult should send event`() {
        val analyticsEvent = SdkQuerySkuDetailsEvents.SdkQuerySkuDetailsResult(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendQuerySkuDetailsResult(arrayListOf())

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendQuerySkuDetailsFailureParsingSkusEvent should send event`() {
        val analyticsEvent = SdkQuerySkuDetailsEvents.SdkQuerySkuDetailsFailureParsingSkus(DEFAULT_MAP)
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendQuerySkuDetailsFailureParsingSkusEvent(arrayListOf(), EMPTY_STRING)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }

    private companion object {
        const val EVENT_CONTEXT = "AnalyticsSDK"

        const val EMPTY_STRING = ""
        const val DEFAULT_INTEGER = 1
        const val DEFAULT_BOOLEAN = true
        val DEFAULT_MAP = mutableMapOf<String, Any>()
    }
}
