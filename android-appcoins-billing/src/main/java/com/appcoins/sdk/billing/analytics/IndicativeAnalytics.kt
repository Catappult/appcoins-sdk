package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.helpers.DeviceInformation
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logInfo
import java.io.Serializable

object IndicativeAnalytics : Serializable {

    var instanceId: String = "" // Instance id from
    var superProperties: MutableMap<String, Any> = HashMap()

    fun setIndicativeSuperProperties(
        packageName: String?,
        versionCode: Int?,
        deviceInformation: DeviceInformation
    ) {
        superProperties[AnalyticsSuperLabels.GAME_PACKAGE_NAME] = packageName ?: ""
        superProperties[AnalyticsSuperLabels.SDK_VERSION_CODE] = versionCode ?: ""
        superProperties[AnalyticsSuperLabels.SDK_PACKAGE] = "android-billing"
        superProperties[AnalyticsSuperLabels.INSTANCE_ID] = instanceId

        // device information:
        superProperties[AnalyticsSuperLabels.OS_VERSION] = deviceInformation.osVersion
        superProperties[AnalyticsSuperLabels.BRAND] = deviceInformation.brand
        superProperties[AnalyticsSuperLabels.MODEL] = deviceInformation.model
        superProperties[AnalyticsSuperLabels.LANGUAGE] = deviceInformation.language
        superProperties[AnalyticsSuperLabels.IS_EMULATOR] = deviceInformation.isProbablyEmulator
    }

    fun updateInstanceId(instanceId: String) {
        logInfo("Update IndicativeID for User.")
        logDebug("New Id: $instanceId")
        this.instanceId = instanceId
        superProperties[AnalyticsSuperLabels.INSTANCE_ID] = instanceId
    }

    fun getLoggableSuperProperties(): String =
        "{probably_emulator=${superProperties[AnalyticsSuperLabels.IS_EMULATOR]}" +
            ", device_model=${superProperties[AnalyticsSuperLabels.MODEL]}" +
            ", device_brand=${superProperties[AnalyticsSuperLabels.BRAND]}" +
            ", os_version=${superProperties[AnalyticsSuperLabels.OS_VERSION]}" +
            ", package_name=${superProperties[AnalyticsSuperLabels.GAME_PACKAGE_NAME]}" +
            ", version_code=${superProperties[AnalyticsSuperLabels.SDK_VERSION_CODE]}" +
            ", sdk_package=${superProperties[AnalyticsSuperLabels.SDK_PACKAGE]}" +
            ", language=${superProperties[AnalyticsSuperLabels.LANGUAGE]}}"
}
