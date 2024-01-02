package com.appcoins.sdk.billing.analytics

import com.appcoins.sdk.billing.helpers.DeviceInformation
import java.io.Serializable

object IndicativeAnalytics : Serializable {

  var instanceId: String = ""  // Instance id from
  var superProperties: MutableMap<String, Any> = HashMap()

  fun setIndicativeSuperProperties(
    packageName: String?,
    versionCode: Int?,
    skuDetails: String?,
    deviceInformation: DeviceInformation
  ) {
    superProperties[AnalyticsSuperLabels.GAME_PACKAGE_NAME] = packageName ?: ""
    superProperties[AnalyticsSuperLabels.SDK_VERSION_CODE] = versionCode ?: ""
    superProperties[AnalyticsSuperLabels.SDK_PACKAGE] = "android-billing"
    superProperties[AnalyticsSuperLabels.SKU_NAME] = skuDetails ?: ""
    superProperties[AnalyticsSuperLabels.INSTANCE_ID] = instanceId

    // device information:
    superProperties[AnalyticsSuperLabels.OS_VERSION] = deviceInformation.osVersion
    superProperties[AnalyticsSuperLabels.BRAND] = deviceInformation.brand
    superProperties[AnalyticsSuperLabels.MODEL] = deviceInformation.model
    superProperties[AnalyticsSuperLabels.LANGUAGE] = deviceInformation.language
    superProperties[AnalyticsSuperLabels.IS_EMULATOR] = deviceInformation.isProbablyEmulator
  }
}
