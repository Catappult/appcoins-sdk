package com.appcoins.sdk.core.analytics.indicative

import com.appcoins.sdk.core.analytics.AnalyticsContent
import com.appcoins.sdk.core.device.DeviceInformation
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logInfo
import java.io.Serializable

object IndicativeAnalytics : Serializable {

    var instanceId: String = "" // Instance id from
    var superProperties: MutableMap<String, Any> = HashMap()

    fun setupIndicativeProperties(
        packageName: String?,
        versionCode: Int?,
        deviceInformation: DeviceInformation,
        instanceId: String,
    ) {
        this.instanceId = instanceId
        superProperties[AnalyticsContent.GAME_PACKAGE_NAME] = packageName ?: ""
        superProperties[AnalyticsContent.SDK_VERSION_CODE] = versionCode ?: ""
        superProperties[AnalyticsContent.SDK_PACKAGE] = "android-billing"
        superProperties[AnalyticsContent.INSTANCE_ID] = instanceId

        // device information:
        superProperties[AnalyticsContent.OS_VERSION] = deviceInformation.osVersion
        superProperties[AnalyticsContent.BRAND] = deviceInformation.brand
        superProperties[AnalyticsContent.MODEL] = deviceInformation.model
        superProperties[AnalyticsContent.LANGUAGE] = deviceInformation.language
        superProperties[AnalyticsContent.IS_EMULATOR] = deviceInformation.isProbablyEmulator
    }

    fun updateInstanceId(instanceId: String) {
        logInfo("Update IndicativeID for User.")
        logDebug("New Id: $instanceId")
        this.instanceId = instanceId
        superProperties[AnalyticsContent.INSTANCE_ID] = instanceId
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
