package com.appcoins.sdk.billing.analytics

import android.content.Context
import android.content.res.Configuration
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.DeviceInformation
import java.util.HashMap

class IndicativeAnalytics constructor(
   private val context: Context
)  {

  var instanceId: String = ""  // Instance id from
  var superProperties: MutableMap<String, Any> = HashMap()

  companion object {
    const val ORIENTATION_PORTRAIT = "portrait"
    const val ORIENTATION_LANDSCAPE = "landscape"
    const val ORIENTATION_OTHER = "other"
  }

  fun setIndicativeSuperProperties(
    packageName: String?,
    versionCode: Int?,
    skuDetails: String?,
    deviceInformation: DeviceInformation
  ) {

    val libraryPackageName = BuildConfig.LIBRARY_PACKAGE_NAME;
    if (BuildConfig.DEBUG) {
      libraryPackageName.plus(".dev")
    }

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

  fun findDeviceOrientation(): String {
    return when (context.resources.configuration.orientation) {
      Configuration.ORIENTATION_LANDSCAPE -> ORIENTATION_LANDSCAPE
      Configuration.ORIENTATION_PORTRAIT -> ORIENTATION_PORTRAIT
      Configuration.ORIENTATION_UNDEFINED -> ORIENTATION_OTHER
      else -> ORIENTATION_OTHER
    }
  }
}
