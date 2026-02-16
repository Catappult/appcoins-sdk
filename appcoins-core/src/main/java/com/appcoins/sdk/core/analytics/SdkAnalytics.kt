package com.appcoins.sdk.core.analytics

import android.content.Context
import com.appcoins.sdk.core.analytics.events.AnalyticsEvent
import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableEvents
import com.appcoins.sdk.core.analytics.events.SdkAppUpdateAvailableLabels
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestEvents
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestLabels
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseEvents
import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseLabels
import com.appcoins.sdk.core.analytics.events.SdkFeatureFlagEvents
import com.appcoins.sdk.core.analytics.events.SdkFeatureFlagLabels
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureEvents
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureLabels
import com.appcoins.sdk.core.analytics.events.SdkGeneralFailureStep
import com.appcoins.sdk.core.analytics.events.SdkGetReferralDeeplinkEvents
import com.appcoins.sdk.core.analytics.events.SdkGetReferralDeeplinkLabels
import com.appcoins.sdk.core.analytics.events.SdkInitializationEvents
import com.appcoins.sdk.core.analytics.events.SdkInitializationLabels
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogEvents
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogLabels
import com.appcoins.sdk.core.analytics.events.SdkIsFeatureSupportedEvents
import com.appcoins.sdk.core.analytics.events.SdkIsFeatureSupportedLabels
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateDialogEvents
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateDialogLabels
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateEvents
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateLabels
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateStoreEvents
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowLabels
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesEvents
import com.appcoins.sdk.core.analytics.events.SdkQueryPurchasesLabels
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsEvents
import com.appcoins.sdk.core.analytics.events.SdkQuerySkuDetailsLabels
import com.appcoins.sdk.core.analytics.events.SdkSystemInformationEvents
import com.appcoins.sdk.core.analytics.events.SdkWalletPaymentFlowEvents
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowLabels
import com.appcoins.sdk.core.analytics.manager.AnalyticsManager
import com.appcoins.sdk.core.analytics.severity.SdkAnalyticsSeverityUtils
import com.appcoins.sdk.core.network.NetworkTraffic
import org.json.JSONArray
import org.json.JSONObject

@Suppress("complexity:TooManyFunctions")
class SdkAnalytics(private val analyticsManager: AnalyticsManager) {

    private var eventsQueue = arrayListOf<AnalyticsEvent>()

    companion object {
        private const val EVENT_CONTEXT = "AnalyticsSDK"
    }

    fun sendEventsOnQueue() {
        eventsQueue.forEach {
            logEvent(it)
        }
        eventsQueue.clear()
    }

    // App Update Available events
    fun sendAppUpdateAvailableRequest() {
        logEvent(SdkAppUpdateAvailableEvents.SdkAppUpdateAvailableRequest())
    }

    fun sendAppUpdateAvailableResult(result: Boolean) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkAppUpdateAvailableLabels.RESULT] = result

        logEvent(SdkAppUpdateAvailableEvents.SdkAppUpdateAvailableResult(eventData))
    }

    fun sendAppUpdateAvailableMainThreadFailure() {
        logEvent(SdkAppUpdateAvailableEvents.SdkAppUpdateAvailableMainThreadFailure())
    }

    fun sendAppUpdateAvailableFailureToObtainResult() {
        logEvent(SdkAppUpdateAvailableEvents.SdkAppUpdateAvailableFailureToObtainResult())
    }

    // Backend Requests events
    fun sendBackendRequestEvent(
        type: SdkBackendRequestType,
        url: String,
        method: String,
        paths: List<String>?,
        header: Map<String, String>?,
        queries: Map<String, String>?,
        body: Map<String, Any>?,
    ) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkBackendRequestLabels.TYPE] = type.type

        addBackendRequestData(eventData, url, method, paths, header, queries, body)

        logEvent(SdkBackendRequestEvents.SdkCallBackendRequest(eventData))
    }

    fun sendBackendResponseEvent(
        type: SdkBackendRequestType,
        responseCode: Int?,
        responseMessage: String?,
        errorMessage: String? = null
    ) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkBackendRequestLabels.TYPE] = type.type

        addBackendResponseData(eventData, responseCode, responseMessage, errorMessage)

        logEvent(SdkBackendRequestEvents.SdkCallBackendResponse(eventData))
    }

    fun sendBackendErrorEvent(type: SdkBackendRequestType, url: String, responseMessage: String?, context: Context) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkBackendRequestLabels.TYPE] = type.type
        eventData[SdkBackendRequestLabels.URL] = url
        eventData[SdkBackendRequestLabels.RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[SdkBackendRequestLabels.NETWORK_SPEED] = NetworkTraffic().getAverageSpeed(context) ?: "null"

        logEvent(SdkBackendRequestEvents.SdkCallBackendError(eventData))
    }

    fun sendBackendMappingFailureEvent(
        type: SdkBackendRequestType,
        responseMessage: String?,
        errorMessage: String? = null,
    ) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkBackendRequestLabels.TYPE] = type.type
        eventData[SdkBackendRequestLabels.RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[SdkBackendRequestLabels.ERROR_MESSAGE] = errorMessage ?: ""

        logEvent(SdkBackendRequestEvents.SdkCallBackendMappingFailure(eventData))
    }

    fun sendBackendDnsManualCacheSuccessEvent(url: String, ip: String, responseCode: Int) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkBackendRequestLabels.URL] = url
        eventData[SdkBackendRequestLabels.IP_ADDRESS] = ip
        eventData[SdkBackendRequestLabels.RESPONSE_CODE] = responseCode

        logEvent(SdkBackendRequestEvents.SdkCallBackendDnsManualCacheSuccess(eventData))
    }

    // Consume Purchase events
    fun sendConsumePurchaseRequest(purchaseToken: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        purchaseToken?.let { eventData[SdkConsumePurchaseLabels.PURCHASE_TOKEN] = it }

        logEvent(SdkConsumePurchaseEvents.SdkConsumePurchaseRequest(eventData))
    }

    fun sendConsumePurchaseResult(purchaseToken: String?, responseCode: Int) {
        val eventData: MutableMap<String, Any> = HashMap()

        purchaseToken?.let { eventData[SdkConsumePurchaseLabels.PURCHASE_TOKEN] = it }
        eventData[SdkConsumePurchaseLabels.RESPONSE_CODE] = responseCode

        logEvent(SdkConsumePurchaseEvents.SdkConsumePurchaseResult(eventData))
    }

    // Feature Flag events
    fun sendRequestLimitTriggered(sdkRequestType: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkFeatureFlagLabels.SDK_REQUEST_TYPE] = sdkRequestType

        logEvent(SdkFeatureFlagEvents.SdkRequestLimitTriggered(eventData))
    }

    fun sendPurchaseRequestLimitTriggered() {
        logEvent(SdkFeatureFlagEvents.SdkPurchaseRequestLimitTriggered())
    }

    fun sendMMPPurchaseEventRecovered(orderId: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkFeatureFlagLabels.ORDER_ID] = orderId

        logEvent(SdkFeatureFlagEvents.SdkMMPPurchaseEventRecovered(eventData))
    }

    // General Failures events
    fun sendServiceConnectionExceptionEvent(step: SdkGeneralFailureStep) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkGeneralFailureLabels.STEP] = step.type

        logEvent(SdkGeneralFailureEvents.SdkServiceConnectionException(eventData))
    }

    fun sendPurchaseSignatureVerificationFailureEvent(purchaseToken: String, apiKey: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkGeneralFailureLabels.SIGNED_DATA] = purchaseToken
        apiKey?.let { eventData[SdkGeneralFailureLabels.API_KEY] = it }

        logEvent(SdkGeneralFailureEvents.SdkPurchaseSignatureVerificationFailure(eventData))
    }

    fun sendUnexpectedFailureEvent(type: String, data: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkGeneralFailureLabels.TYPE] = type
        eventData[SdkGeneralFailureLabels.DATA] = data

        logEvent(SdkGeneralFailureEvents.SdkUnexpectedFailure(eventData))
    }

    // Get Referral Deeplink events
    fun sendGetReferralDeeplinkRequestEvent() {
        logEvent(SdkGetReferralDeeplinkEvents.SdkGetReferralDeeplinkRequest())
    }

    fun sendGetReferralDeeplinkResultEvent(deeplink: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkGetReferralDeeplinkLabels.DEEPLINK] = deeplink ?: "null"

        logEvent(SdkGetReferralDeeplinkEvents.SdkGetReferralDeeplinkResult(eventData))
    }

    fun sendGetReferralDeeplinkMainThreadFailureEvent() {
        logEvent(SdkGetReferralDeeplinkEvents.SdkGetReferralDeeplinkMainThreadFailure())
    }

    // Is Feature Supported events
    fun sendIsFeatureSupportedRequestEvent(feature: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkIsFeatureSupportedLabels.FEATURE] = feature

        logEvent(SdkIsFeatureSupportedEvents.SdkIsFeatureSupportedRequest(eventData))
    }

    fun sendIsFeatureSupportedResultEvent(result: Int) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkIsFeatureSupportedLabels.RESULT] = result

        logEvent(SdkIsFeatureSupportedEvents.SdkIsFeatureSupportedResult(eventData))
    }

    // Initialization events
    fun sendStartConnectionEvent() {
        logEvent(SdkInitializationEvents.SdkStartConnection())
    }

    fun sendServiceConnectedEvent(service: String, method: String? = null) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkInitializationLabels.SERVICE] = service
        method?.let { eventData[SdkInitializationLabels.METHOD] = it }

        logEvent(SdkInitializationEvents.SdkServiceConnected(eventData))
    }

    fun sendServiceConnectionFailureEvent(service: String, method: String? = null) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkInitializationLabels.SERVICE] = service
        method?.let { eventData[SdkInitializationLabels.METHOD] = it }

        logEvent(SdkInitializationEvents.SdkServiceConnectionFailure(eventData))
    }

    fun sendFinishConnectionEvent() {
        logEvent(SdkInitializationEvents.SdkFinishConnection())
    }

    fun sendAttributionRequestEvent() {
        logEvent(SdkInitializationEvents.SdkAttributionRequest())
    }

    fun sendAttributionResultEvent(
        oemid: String?,
        guestId: String?,
        utmSource: String?,
        utmMedium: String?,
        utmCampaign: String?,
        utmTerm: String?,
        utmContent: String?,
    ) {
        val eventData: MutableMap<String, Any> = HashMap()

        oemid?.let { eventData[SdkInitializationLabels.OEMID] = it }
        guestId?.let { eventData[SdkInitializationLabels.GUEST_ID] = it }
        utmSource?.let { eventData[SdkInitializationLabels.UTM_SOURCE] = it }
        utmMedium?.let { eventData[SdkInitializationLabels.UTM_MEDIUM] = it }
        utmCampaign?.let { eventData[SdkInitializationLabels.UTM_CAMPAIGN] = it }
        utmTerm?.let { eventData[SdkInitializationLabels.UTM_TERM] = it }
        utmContent?.let { eventData[SdkInitializationLabels.UTM_CONTENT] = it }

        logEvent(SdkInitializationEvents.SdkAttributionResult(eventData))
    }

    fun sendAttributionRetryAttemptEvent(message: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkInitializationLabels.MESSAGE] = message

        logEvent(SdkInitializationEvents.SdkAttributionRetryAttempt(eventData))
    }

    fun sendAttributionRequestFailureEvent() {
        logEvent(SdkInitializationEvents.SdkAttributionRequestFailure())
    }

    fun sendPayflowRequestEvent() {
        logEvent(SdkInitializationEvents.SdkPayflowRequest())
    }

    fun sendPayflowResultEvent(paymentFlowMethods: List<String>?) {
        val eventData: MutableMap<String, Any> = HashMap()

        paymentFlowMethods?.let {
            val jsonArray = JSONArray()
            it.forEach { paymentFlowMethod ->
                jsonArray.put(paymentFlowMethod)
            }
            eventData[SdkInitializationLabels.PAYMENT_FLOW_LIST] = jsonArray.toString()
        }

        logEvent(SdkInitializationEvents.SdkPayflowResult(eventData))
    }

    fun sendAppInstallationTriggerEvent(appPackageName: String, state: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkInitializationLabels.APP_PACKAGE_NAME] = appPackageName
        eventData[SdkInitializationLabels.STATE] = state

        logEvent(SdkInitializationEvents.SdkAppInstallationTrigger(eventData))
    }

    // Wallet Install Dialog events
    fun sendInstallWalletDialogEvent() {
        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialog())
    }

    fun sendInstallWalletDialogActionEvent(action: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[SdkInstallWalletDialogLabels.WALLET_INSTALL_ACTION] = action

        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialogAction(eventData))
    }

    fun sendInstallWalletDialogDownloadWalletVanillaEvent() {
        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialogDownloadWalletVanilla())
    }

    fun sendInstallWalletDialogDownloadWalletFallbackEvent(source: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkInstallWalletDialogLabels.SOURCE] = source

        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialogDownloadWalletFallback(eventData))
    }

    fun sendInstallWalletDialogSuccessEvent() {
        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialogSuccess())
    }

    // Launch App Update events
    fun sendLaunchAppUpdateResultEvent(deeplink: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[SdkLaunchAppUpdateLabels.DEEPLINK] = deeplink

        logEvent(SdkLaunchAppUpdateEvents.SdkLaunchAppUpdateResult(eventData))
    }

    fun sendLaunchAppUpdateDeeplinkFailureEvent(deeplink: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[SdkLaunchAppUpdateLabels.DEEPLINK] = deeplink

        logEvent(SdkLaunchAppUpdateEvents.SdkLaunchAppUpdateDeeplinkFailure(eventData))
    }

    // Launch App Update Dialog events
    fun sendLaunchAppUpdateDialogRequestEvent() {
        logEvent(SdkLaunchAppUpdateDialogEvents.SdkLaunchAppUpdateDialogRequest())
    }

    fun sendLaunchAppUpdateDialogActionEvent(action: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[SdkLaunchAppUpdateDialogLabels.ACTION] = action

        logEvent(SdkLaunchAppUpdateDialogEvents.SdkLaunchAppUpdateDialogAction(eventData))
    }

    // Launch App Update Store events
    fun sendLaunchAppUpdateStoreRequestEvent() {
        logEvent(SdkLaunchAppUpdateStoreEvents.SdkLaunchAppUpdateStoreRequest())
    }

    // Purchase Flow Events
    fun sendLaunchPurchaseEvent(
        sku: String,
        skuType: String,
        developerPayload: String?,
        orderReference: String?,
        origin: String?,
        obfuscatedAccountId: String?,
        freeTrial: Boolean?,
    ) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[SdkPurchaseFlowLabels.SKU] = sku
        eventData[SdkPurchaseFlowLabels.SKU_TYPE] = skuType
        developerPayload?.let { eventData[SdkPurchaseFlowLabels.DEVELOPER_PAYLOAD] = it }
        orderReference?.let { eventData[SdkPurchaseFlowLabels.ORDER_REFERENCE] = it }
        origin?.let { eventData[SdkPurchaseFlowLabels.ORIGIN] = it }
        obfuscatedAccountId?.let { eventData[SdkPurchaseFlowLabels.OBFUSCATED_ACCOUNT_ID] = it }
        freeTrial?.let { eventData[SdkPurchaseFlowLabels.FREE_TRIAL] = it }

        logEvent(SdkPurchaseFlowEvents.SdkLaunchPurchase(eventData))
    }

    fun sendPurchaseResultEvent(
        responseCode: Int,
        purchaseToken: String? = null,
        sku: String? = null,
        failureMessage: String? = null
    ) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkPurchaseFlowLabels.RESPONSE_CODE] = responseCode
        purchaseToken?.let { eventData[SdkPurchaseFlowLabels.PURCHASE_TOKEN] = it }
        sku?.let { eventData[SdkPurchaseFlowLabels.SKU] = it }
        failureMessage?.let { eventData[SdkPurchaseFlowLabels.FAILURE_MESSAGE] = it }

        logEvent(SdkPurchaseFlowEvents.SdkPurchaseResult(eventData))
    }

    fun sendLaunchPurchaseTypeNotSupportedFailureEvent(skuType: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkPurchaseFlowLabels.SKU_TYPE] = skuType

        logEvent(SdkPurchaseFlowEvents.SdkLaunchPurchaseTypeNotSupportedFailure(eventData))
    }

    fun sendLaunchPurchaseMainThreadFailureEvent() {
        logEvent(SdkPurchaseFlowEvents.SdkLaunchPurchaseMainThreadFailure())
    }

    // Web Payment Events
    fun sendWebPaymentStartEvent(url: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.URL] = url

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentStart(eventData))
    }

    fun sendWebPaymentFailureToObtainUrlEvent() {
        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentFailureToObtainUrl())
    }

    fun sendWebPaymentFailureToOpenDeeplinkEvent(deeplink: String, exception: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.DEEPLINK] = deeplink
        eventData[SdkWebPaymentFlowLabels.EXCEPTION] = exception

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentFailureToOpenDeeplink(eventData))
    }

    fun sendWebPaymentErrorProcessingPurchaseResultEvent(result: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.RESULT] = result

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentErrorProcessingPurchaseResult(eventData))
    }

    fun sendWebPaymentPurchaseResultEmptyEvent() {
        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentPurchaseResultEmpty())
    }

    fun sendWebPaymentOpenDeeplinkEvent(deeplink: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.DEEPLINK] = deeplink

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentOpenDeeplink(eventData))
    }

    fun sendWebPaymentLaunchExternalPaymentEvent(url: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.URL] = url

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentLaunchExternalPayment(eventData))
    }

    fun sendWebPaymentAllowExternalAppsEvent(allow: Boolean) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.ALLOW] = allow

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentAllowExternalApps(eventData))
    }

    fun sendWebPaymentCloseBehaviorEvent(configJson: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.CONFIG] = configJson

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentUpdateCloseBehavior(eventData))
    }

    fun sendWebPaymentExternalPaymentResultEvent() {
        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentExternalPaymentResult())
    }

    fun sendWebPaymentExecuteExternalDeeplinkEvent(deeplink: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        deeplink?.let { eventData[SdkWebPaymentFlowLabels.DEEPLINK] = it }

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentExecuteExternalDeeplink(eventData))
    }

    fun sendWebPaymentWalletPaymentResultEvent() {
        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentWalletPaymentResult())
    }

    // Wallet Payment events
    fun sendWalletPaymentStartEvent() {
        logEvent(SdkWalletPaymentFlowEvents.SdkWalletPaymentStart())
    }

    fun sendWalletPaymentEmptyDataEvent() {
        logEvent(SdkWalletPaymentFlowEvents.SdkWalletPaymentEmptyData())
    }

    // Query Purchases events
    fun sendQueryPurchasesRequestEvent(skuType: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkQueryPurchasesLabels.SKU_TYPE] = skuType

        logEvent(SdkQueryPurchasesEvents.SdkQueryPurchasesRequest(eventData))
    }

    fun sendQueryPurchasesTypeNotSupportedErrorEvent(skuType: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkQueryPurchasesLabels.SKU_TYPE] = skuType

        logEvent(SdkQueryPurchasesEvents.SdkQueryPurchasesTypeNotSupportedError(eventData))
    }

    fun sendQueryPurchasesResultEvent(purchases: List<String>?) {
        val eventData: MutableMap<String, Any> = HashMap()

        purchases?.let {
            val jsonArray = JSONArray()
            it.forEach { purchaseToken ->
                jsonArray.put(purchaseToken)
            }
            eventData[SdkQueryPurchasesLabels.PURCHASES] = jsonArray.toString()
        }

        logEvent(SdkQueryPurchasesEvents.SdkQueryPurchasesResult(eventData))
    }

    // Query SKU Details events
    fun sendQuerySkuDetailsRequestEvent(skus: List<String>?, skuType: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        skus?.let {
            val jsonArray = JSONArray()
            it.forEach { sku ->
                jsonArray.put(sku)
            }
            eventData[SdkQuerySkuDetailsLabels.SKUS] = jsonArray.toString()
        }
        eventData[SdkQuerySkuDetailsLabels.SKU_TYPE] = skuType

        logEvent(SdkQuerySkuDetailsEvents.SdkQuerySkuDetailsRequest(eventData))
    }

    fun sendQuerySkuDetailsResult(skus: List<String>?) {
        val eventData: MutableMap<String, Any> = HashMap()

        skus?.let {
            val jsonArray = JSONArray()
            it.forEach { sku ->
                jsonArray.put(sku)
            }
            eventData[SdkQuerySkuDetailsLabels.SKUS] = jsonArray.toString()
        }

        logEvent(SdkQuerySkuDetailsEvents.SdkQuerySkuDetailsResult(eventData))
    }

    fun sendQuerySkuDetailsFailureParsingSkusEvent(skus: List<String>?, skuType: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        skus?.let {
            val jsonArray = JSONArray()
            it.forEach { sku ->
                jsonArray.put(sku)
            }
            eventData[SdkQuerySkuDetailsLabels.SKUS] = jsonArray.toString()
        }
        eventData[SdkQuerySkuDetailsLabels.SKU_TYPE] = skuType

        logEvent(SdkQuerySkuDetailsEvents.SdkQuerySkuDetailsFailureParsingSkus(eventData))
    }

    // System Information events
    fun sendDoNotKeepActivitiesEvent() {
        logEvent(SdkSystemInformationEvents.SdkDoNotKeepActivitiesActive())
    }

    private fun addBackendRequestData(
        eventData: MutableMap<String, Any>,
        url: String,
        method: String,
        paths: List<String>?,
        header: Map<String, String>?,
        queries: Map<String, String>?,
        body: Map<String, Any>?,
    ): MutableMap<String, Any> {
        eventData[SdkBackendRequestLabels.URL] = url
        eventData[SdkBackendRequestLabels.METHOD] = method
        paths?.let {
            val jsonArray = JSONArray()
            it.forEach { path ->
                jsonArray.put(path)
            }
            if (jsonArray.length() > 0) {
                eventData[SdkBackendRequestLabels.PATHS] = jsonArray.toString()
            }
        }
        header?.let {
            val jsonArray = JSONArray()
            it.forEach { header ->
                val jsonObject = JSONObject()
                jsonObject.put(header.key, header.value)
                jsonArray.put(jsonObject)
            }
            if (jsonArray.length() > 0) {
                eventData[SdkBackendRequestLabels.HEADERS] = jsonArray.toString()
            }
        }
        queries?.let {
            val jsonArray = JSONArray()
            it.forEach { header ->
                val jsonObject = JSONObject()
                jsonObject.put(header.key, header.value)
                jsonArray.put(jsonObject)
            }
            if (jsonArray.length() > 0) {
                eventData[SdkBackendRequestLabels.QUERIES] = jsonArray.toString()
            }
        }
        body?.let {
            val jsonArray = JSONArray()
            it.forEach { bodyValue ->
                val jsonObject = JSONObject()
                jsonObject.put(bodyValue.key, bodyValue.value)
                jsonArray.put(jsonObject)
            }
            if (jsonArray.length() > 0) {
                eventData[SdkBackendRequestLabels.BODY] = jsonArray.toString()
            }
        }

        return eventData
    }

    private fun addBackendResponseData(
        eventData: MutableMap<String, Any>,
        responseCode: Int?,
        responseMessage: String?,
        errorMessage: String? = null
    ): MutableMap<String, Any> {
        eventData[SdkBackendRequestLabels.RESPONSE_CODE] = responseCode?.toString() ?: ""
        eventData[SdkBackendRequestLabels.RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[SdkBackendRequestLabels.ERROR_MESSAGE] = errorMessage ?: ""

        return eventData
    }

    private fun logEvent(analyticsEvent: AnalyticsEvent) {
        if (!SdkAnalyticsUtils.isAnalyticsSetupFromPayflowFinalized) {
            eventsQueue.add(analyticsEvent)
            return
        }
        if (SdkAnalyticsSeverityUtils().isEventSeverityAllowed(analyticsEvent)) {
            analyticsManager.logEvent(
                analyticsEvent.data,
                analyticsEvent.name,
                analyticsEvent.action,
                EVENT_CONTEXT
            )
        }
    }
}
