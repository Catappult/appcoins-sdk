package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.core.network.NetworkTraffic
import org.json.JSONArray
import org.json.JSONObject

@Suppress("complexity:TooManyFunctions")
class SdkAnalytics(private val analyticsManager: AnalyticsManager) {

    companion object {
        private const val EVENT_CONTEXT = "AnalyticsSDK"
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

    fun sendBackendErrorEvent(type: SdkBackendRequestType, url: String, responseMessage: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkBackendRequestLabels.TYPE] = type.type
        eventData[SdkBackendRequestLabels.URL] = url
        eventData[SdkBackendRequestLabels.RESPONSE_MESSAGE] = responseMessage ?: ""
        eventData[AnalyticsLabels.NETWORK_SPEED] = NetworkTraffic().getAverageSpeed(WalletUtils.context) ?: "null"

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

        logEvent(SdkBackendRequestEvents.SdkCallBackendError(eventData))
    }

    // Consume Purchase events
    fun sendConsumePurchaseRequest(purchaseToken: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkConsumePurchaseLabels.PURCHASE_TOKEN] = purchaseToken

        logEvent(SdkConsumePurchaseEvents.SdkConsumePurchaseRequest(eventData))
    }

    fun sendConsumePurchaseResult(purchaseToken: String, responseCode: Int) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkConsumePurchaseLabels.PURCHASE_TOKEN] = purchaseToken
        eventData[SdkConsumePurchaseLabels.RESPONSE_CODE] = responseCode

        logEvent(SdkConsumePurchaseEvents.SdkConsumePurchaseResult(eventData))
    }

    // General Failures events
    fun sendServiceConnectionExceptionEvent(step: SdkGeneralFailureStep) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkGeneralFailureLabels.STEP] = step.type

        logEvent(SdkGeneralFailureEvents.SdkServiceConnectionException(eventData))
    }

    fun sendPurchaseSignatureVerificationFailureEvent(purchaseToken: String, apiKey: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkGeneralFailureLabels.PURCHASE_TOKEN] = purchaseToken
        eventData[SdkGeneralFailureLabels.API_KEY] = apiKey

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

    fun sendGetReferralDeeplinkResultEvent(deeplink: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkGetReferralDeeplinkLabels.DEEPLINK] = deeplink

        logEvent(SdkGetReferralDeeplinkEvents.SdkGetReferralDeeplinkResult(eventData))
    }

    fun sendGetReferralDeeplinkMainThreadFailureEvent() {
        logEvent(SdkGetReferralDeeplinkEvents.SdkGetReferralDeeplinkMainThreadFailure())
    }

    // Initialization events
    fun sendStartConnectionEvent() {
        logEvent(SdkInitializationEvents.SdkStartConnection())
    }

    fun sendServiceConnectedEvent(service: SdkInitializationService, method: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkInitializationLabels.SERVICE] = service.type
        method?.let { eventData[SdkInitializationLabels.METHOD] = it }

        logEvent(SdkInitializationEvents.SdkServiceConnected(eventData))
    }

    fun sendServiceConnectionFailureEvent(reason: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        reason?.let { eventData[SdkInitializationLabels.REASON] = it }

        logEvent(SdkInitializationEvents.SdkServiceConnectionFailure(eventData))
    }

    fun sendFinishConnectionEvent(service: SdkInitializationService, method: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkInitializationLabels.SERVICE] = service.type
        method?.let { eventData[SdkInitializationLabels.METHOD] = it }

        logEvent(SdkInitializationEvents.SdkFinishConnection(eventData))
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

    fun sendAttributionRequestFailureEvent(reason: String?) {
        val eventData: MutableMap<String, Any> = HashMap()

        reason?.let { eventData[SdkInitializationLabels.REASON] = it }

        logEvent(SdkInitializationEvents.SdkAttributionRequestFailure(eventData))
    }

    fun sendAttributionRetryAttemptEvent() {
        logEvent(SdkInitializationEvents.SdkAttributionRetryAttempt())
    }

    fun sendBackendGuestUidGenerationFailedEvent() {
        val eventData: MutableMap<String, Any> = HashMap()
        // TODO: Use correct Data values.
        eventData[AnalyticsLabels.PAYMENT_STATUS] = failureMessage
        logEvent(SdkInitializationEvents.SdkAttributionRequestFailure(eventData))
    }

    fun sendPayflowRequestEvent() {
        logEvent(SdkInitializationEvents.SdkPayflowRequest())
    }

    fun sendPayflowResultEvent(paymentFlowMethods: List<PaymentFlowMethod>?) {
        val eventData: MutableMap<String, Any> = HashMap()

        paymentFlowMethods?.let {
            val jsonArray = JSONArray()
            it.forEach { paymentFlowMethod ->
                jsonArray.put(paymentFlowMethod.name)
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
    fun walletInstallImpression() {
        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialog())
    }

    fun walletInstallClick(action: String) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[SdkInstallWalletDialogLabels.WALLET_INSTALL_ACTION] = action

        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialogAction(eventData))
    }

    fun downloadWalletAptoideImpression() {
        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialogVanillaImpression())
    }

    fun downloadWalletFallbackImpression() {
        logEvent(SdkInstallWalletDialogEvents.SdkInstallWalletDialogFallbackImpression())
    }

    fun installWalletAptoideSuccess() {
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
    fun sendLaunchAppUpdateStoreRequestEvent(deeplink: String) {
        logEvent(SdkLaunchAppUpdateStoreEvents.SdkLaunchAppUpdateStoreRequest())
    }

    // Purchase Flow Events
    fun sendLaunchPurchaseRequestEvent(
        sku: String,
        skuType: String,
        developerPayload: String?,
        orderReference: String?,
        origin: String,
    ) {
        val eventData: MutableMap<String, Any> = HashMap()
        eventData[SdkPurchaseFlowLabels.SKU] = sku
        eventData[SdkPurchaseFlowLabels.SKU_TYPE] = skuType
        developerPayload?.let { eventData[SdkPurchaseFlowLabels.DEVELOPER_PAYLOAD] = it }
        orderReference?.let { eventData[SdkPurchaseFlowLabels.ORDER_REFERENCE] = it }
        eventData[SdkPurchaseFlowLabels.ORIGIN] = origin

        logEvent(SdkPurchaseFlowEvents.SdkLaunchPurchase(eventData))
    }

    fun sendPurchaseResultEvent(responseCode: Int, purchaseToken: String, sku: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkPurchaseFlowLabels.RESPONSE_CODE] = responseCode
        eventData[SdkPurchaseFlowLabels.PURCHASE_TOKEN] = purchaseToken
        eventData[SdkPurchaseFlowLabels.SKU] = sku

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

    fun sendWebPaymentUrlNotGeneratedEvent() {
        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentFailureToObtainUrl())
    }

    fun sendWebPaymentStartEvent(deeplink: String, exception: String) {
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

    fun sendWebPaymentLaunchExternalPaymentEvent(allow: Boolean) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.ALLOW] = allow

        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentAllowExternalApps(eventData))
    }

    fun sendWebPaymentExternalPaymentResultEvent() {
        logEvent(SdkWebPaymentFlowEvents.SdkWebPaymentExternalPaymentResult())
    }

    fun sendWebPaymentExecuteExternalDeeplinkEvent(deeplink: String) {
        val eventData: MutableMap<String, Any> = HashMap()

        eventData[SdkWebPaymentFlowLabels.DEEPLINK] = deeplink

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

    fun sendQueryPurchasesTypeNotSupportedErrorEvent(purchases: List<String>?) {
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

    fun sendQuerySkuDetailsRequestEvent() {
        logEvent(SdkQuerySkuDetailsEvents.SdkQuerySkuDetailsNoSkusPresentFailure())
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
