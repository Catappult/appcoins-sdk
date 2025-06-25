package com.appcoins.sdk.core.analytics

import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseEvents.CONSUME_PURCHASE_FLOW
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogEvents.INSTALL_WALLET_DIALOG_FLOW
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents.PURCHASE_FLOW
import com.appcoins.sdk.core.analytics.events.SdkWalletPaymentFlowEvents.WALLET_PAYMENT_FLOW
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.WEB_PAYMENT_FLOW
import com.appcoins.sdk.core.analytics.matomo.models.CustomProperty
import com.appcoins.sdk.core.analytics.severity.AnalyticsFlowSeverityLevel
import com.appcoins.sdk.core.device.DeviceInformation
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logInfo

object SdkAnalyticsUtils {
    var analyticsFlowSeverityLevels: List<AnalyticsFlowSeverityLevel>? = null
        set(value) {
            field = value
            logInfo(value.toString())
        }
    var matomoCustomProperties: List<CustomProperty>? = null
        set(value) {
            field = value
            logInfo(value.toString())
        }
    val defaultAnalyticsFlowSeverityLevels =
        listOf(
            AnalyticsFlowSeverityLevel(flow = CONSUME_PURCHASE_FLOW, 1),
            AnalyticsFlowSeverityLevel(flow = PURCHASE_FLOW, 1),
            AnalyticsFlowSeverityLevel(flow = WALLET_PAYMENT_FLOW, 1),
            AnalyticsFlowSeverityLevel(flow = WEB_PAYMENT_FLOW, 2),
            AnalyticsFlowSeverityLevel(flow = INSTALL_WALLET_DIALOG_FLOW, 1),
        )

    @Suppress("MagicNumber")
    val defaultMatomoCustomProperties = listOf(
        CustomProperty(1, 1), // SDK Version Code
        CustomProperty(10, 2), // Game Package Name
        CustomProperty(400, 3), // Consume Result
        CustomProperty(410, 4), // Consume Request Purchase Token
        CustomProperty(780, 5), // Service From Service Connection Failure
        CustomProperty(900, 6), // Wallet Install Action
        CustomProperty(1300, 7), // Sku From Launch Purchase
        CustomProperty(1301, 8), // Sku From Purchase Result
        CustomProperty(1370, 9), // Failure Message From Purchase Result
        CustomProperty(1380, 10), // Response Code From Purchase Result
        CustomProperty(1390, 11), // Purchase Token From Purchase Result
        CustomProperty(1600, 12), // Url From Start Web Payment
    )
    var isAnalyticsSetupFromPayflowFinalized: Boolean = false
        set(value) {
            field = value
            if (value) {
                sdkAnalytics.sendEventsOnQueue()
            }
        }
    val sdkAnalytics: SdkAnalytics by lazy { SdkAnalytics(AnalyticsManagerProvider.provideAnalyticsManager()) }
    var isAnalyticsEventLoggerInitialized = false

    var instanceId: String = "" // Instance id from
    var superProperties: MutableMap<String, Any> = HashMap()

    fun setupProperties(
        packageName: String?,
        versionCode: Int?,
        deviceInformation: DeviceInformation,
        instanceId: String,
    ) {
        this.instanceId = instanceId
        superProperties[AnalyticsContent.GAME_PACKAGE_NAME] = packageName ?: ""
        superProperties[AnalyticsContent.SDK_VERSION_CODE] = versionCode ?: ""
        superProperties[AnalyticsContent.SDK_PACKAGE] = "android-billing"

        // device information:
        superProperties[AnalyticsContent.OS_VERSION] = deviceInformation.osVersion
        superProperties[AnalyticsContent.BRAND] = deviceInformation.brand
        superProperties[AnalyticsContent.MODEL] = deviceInformation.model
        superProperties[AnalyticsContent.LANGUAGE] = deviceInformation.language
        superProperties[AnalyticsContent.IS_EMULATOR] = deviceInformation.isProbablyEmulator
    }

    fun updateInstanceId(instanceId: String) {
        logInfo("Update Analytics Instance ID for User.")
        logDebug("New Id: $instanceId")
        this.instanceId = instanceId
    }

    fun getLoggableSuperProperties(): String =
        "{probably_emulator=${superProperties[AnalyticsContent.IS_EMULATOR]}" +
            ", device_model=${superProperties[AnalyticsContent.MODEL]}" +
            ", device_brand=${superProperties[AnalyticsContent.BRAND]}" +
            ", os_version=${superProperties[AnalyticsContent.OS_VERSION]}" +
            ", package_name=${superProperties[AnalyticsContent.GAME_PACKAGE_NAME]}" +
            ", version_code=${superProperties[AnalyticsContent.SDK_VERSION_CODE]}" +
            ", sdk_package=${superProperties[AnalyticsContent.SDK_PACKAGE]}" +
            ", language=${superProperties[AnalyticsContent.LANGUAGE]}}"
}
