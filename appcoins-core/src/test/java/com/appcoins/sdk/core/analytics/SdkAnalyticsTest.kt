package com.appcoins.sdk.core.analytics

import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents
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
    fun `sendStartConnectionEvent should send event`() {
        val analyticsEvent = SdkInitializationEvents.SdkStartConnection()
        every {
            mockkAnalyticsManager.logEvent(
                analyticsEvent.data,
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendStartConnectionEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                analyticsEvent.data,
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }
    /*

    @Test
    fun `sendPurchaseIntentEvent should send event`() {
        val skuName = "test_sku"
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_IAP_PURCHASE_INTENT_START,
                Action.CLICK,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchPurchaseRequestEvent(skuName)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_IAP_PURCHASE_INTENT_START,
                Action.CLICK,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendPurchaseViaWebEvent should send event`() {
        val skuName = "test_sku"
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_WEB_PAYMENT_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentStartEvent(skuName)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_WEB_PAYMENT_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendCallBackendPayflowEvent should send event`() {
        val responseCode = 0
        val responseMessage = "example_message"
        val error = null

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_PAYFLOW,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendCallBackendPayflowEvent(responseCode, responseMessage, error)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_PAYFLOW,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendCallBackendWebPaymentUrlEvent should send event`() {
        val responseCode = 0
        val responseMessage = "example_message"
        val error = null

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_WEB_PAYMENT_URL,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendCallBackendWebPaymentUrlEvent(responseCode, responseMessage, error)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_WEB_PAYMENT_URL,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendCallBackendAttributionEvent should send event`() {
        val responseCode = 0
        val responseMessage = "example_message"
        val error = null

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_ATTRIBUTION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendCallBackendAttributionEvent(responseCode, responseMessage, error)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_ATTRIBUTION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendCallBackendAppVersionEvent should send event`() {
        val responseCode = 0
        val responseMessage = "example_message"
        val error = null

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_APP_VERSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendCallBackendAppVersionEvent(responseCode, responseMessage, error)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_APP_VERSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendCallBackendStoreLinkEvent should send event`() {
        val responseCode = 0
        val responseMessage = "example_message"
        val error = null

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_STORE_LINK,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendCallBackendStoreLinkEvent(responseCode, responseMessage, error)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BACKEND_STORE_LINK,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendCallBindServiceFailEvent should send event`() {
        val priority = 0
        val payflowMethod = "example_payflow_method"

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BINDSERVICE_FAIL,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendCallBindServiceFailEvent(payflowMethod, priority)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkBackendPayflowEvents.SDK_CALL_BINDSERVICE_FAIL,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `walletInstallImpression should send event`() {
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_WALLET_INSTALL_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.walletInstallImpression()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_WALLET_INSTALL_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `walletInstallClick should send event`() {
        val installAction = "example_install_action"

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_WALLET_INSTALL_CLICK,
                Action.CLICK,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.walletInstallClick(installAction)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_WALLET_INSTALL_CLICK,
                Action.CLICK,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `downloadWalletAptoideImpression should send event`() {
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.downloadWalletAptoideImpression()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `downloadWalletFallbackImpression should send event`() {
        val storeType = "example_store_type"

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.downloadWalletFallbackImpression(storeType)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `installWalletAptoideSuccess should send event`() {
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_INSTALL_WALLET_FEEDBACK,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.installWalletAptoideSuccess()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkInstallFlowEvents.SDK_INSTALL_WALLET_FEEDBACK,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `appUpdateDeeplinkImpression should send event`() {
        val deeplink = "example_deeplink"

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkUpdateFlowEvents.SDK_APP_UPDATE_DEEPLINK_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.appUpdateDeeplinkImpression(deeplink)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkUpdateFlowEvents.SDK_APP_UPDATE_DEEPLINK_IMPRESSION,
                Action.IMPRESSION,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `appUpdateImpression should send event`() {
        val sdkLaunchAppUpdateDialogRequest = SdkLaunchAppUpdateDialogEvents.SdkLaunchAppUpdateDialogRequest()
        every {
            mockkAnalyticsManager.logEvent(
                sdkLaunchAppUpdateDialogRequest.data,
                sdkLaunchAppUpdateDialogRequest.name,
                sdkLaunchAppUpdateDialogRequest.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchAppUpdateDialogRequestEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                sdkLaunchAppUpdateDialogRequest.data,
                sdkLaunchAppUpdateDialogRequest.name,
                sdkLaunchAppUpdateDialogRequest.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `appUpdateClick should send event`() {
        val updateAction = "example_update_action"

        val eventData: MutableMap<String, Any> = HashMap()
        eventData[AnalyticsLabels.APP_UPDATE_ACTION] = updateAction

        val sdkLaunchAppUpdateDialogAction = SdkLaunchAppUpdateDialogEvents.SdkLaunchAppUpdateDialogAction(eventData)

        every {
            mockkAnalyticsManager.logEvent(
                sdkLaunchAppUpdateDialogAction.data,
                sdkLaunchAppUpdateDialogAction.name,
                sdkLaunchAppUpdateDialogAction.action,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendLaunchAppUpdateDialogActionEvent(updateAction)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                sdkLaunchAppUpdateDialogAction.data,
                sdkLaunchAppUpdateDialogAction.name,
                sdkLaunchAppUpdateDialogAction.action,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendPurchaseStatusEvent should send event`() {
        val paymentStatus = "example_payment_status"
        val responseMessage = "example_response_message"

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_IAP_PAYMENT_STATUS_FEEDBACK,
                Action.CLICK,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendPurchaseResultEvent(paymentStatus, responseMessage)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_IAP_PAYMENT_STATUS_FEEDBACK,
                Action.CLICK,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendAttributionRetryAttemptEvent should send event`() {
        val failureMessage = "example_failure_message"

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAttributionRetryAttemptEvent(failureMessage)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendUnsuccessfulWebViewResultEvent should send event`() {
        val failureMessage = "example_failure_message"

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentErrorProcessingPurchaseResultEvent(failureMessage)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendWebPaymentUrlNotGeneratedEvent should send event`() {
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendWebPaymentUrlNotGeneratedEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendBackendGuestUidGenerationFailedEvent should send event`() {
        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendAttributionRequestFailureEvent()

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        }
    }

    @Test
    fun `sendUnsuccessfulBackendRequestEvent should send event`() {
        val endpoint = "example_endpoint"
        val failureMessage = "example_failure_message"

        every {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        } just runs

        sdkAnalytics.sendUnsuccessfulBackendRequestEvent(endpoint, failureMessage)

        verify(exactly = 1) {
            mockkAnalyticsManager.logEvent(
                any(),
                SdkAnalyticsEvents.SDK_UNEXPECTED_FAILURE,
                Action.ERROR,
                EVENT_CONTEXT
            )
        }
    }*/

    private companion object {
        const val EVENT_CONTEXT = "AnalyticsSDK"
    }
}
