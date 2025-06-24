package com.appcoins.sdk.core.analytics

import com.appcoins.sdk.core.analytics.events.SdkConsumePurchaseEvents.CONSUME_PURCHASE_FLOW
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogEvents.INSTALL_WALLET_DIALOG_FLOW
import com.appcoins.sdk.core.analytics.events.SdkPurchaseFlowEvents.PURCHASE_FLOW
import com.appcoins.sdk.core.analytics.events.SdkWalletPaymentFlowEvents.WALLET_PAYMENT_FLOW
import com.appcoins.sdk.core.analytics.events.SdkWebPaymentFlowEvents.WEB_PAYMENT_FLOW
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
    var analyticsPropertiesIds: List<Int>? = null
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
    val defaultAnalyticsPropertiesIds = listOf(
        1, // SDK Version Code
        10, // Game Package Name
        400, // Consume Result
        410, // Consume Request Purchase Token
        780, // Service From Service Connection Failure
        900, // Wallet Install Action
        1300, // Sku From Launch Purchase
        1301, // Sku From Purchase Result
        1370, // Failure Message From Purchase Result
        1380, // Response Code From Purchase Result
        1390, // Purchase Token From Purchase Result
        1600, // Url From Start Web Payment
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
