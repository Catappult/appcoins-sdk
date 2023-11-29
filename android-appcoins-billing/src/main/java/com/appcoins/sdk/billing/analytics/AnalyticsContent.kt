package com.appcoins.sdk.billing.analytics

object AnalyticsSuperLabels {
  const val INSTANCE_ID = "instance_id"
  const val SDK_VERSION_CODE = "version_code"
  const val GAME_PACKAGE_NAME = "package_name"
  const val SDK_PACKAGE = "sdk_package"
  const val SKU_NAME = "sku_name"

  const val OS_VERSION = "os_version"
  const val BRAND = "device_brand"
  const val MODEL = "device_model"
  const val LANGUAGE = "language"
  const val IS_EMULATOR = "probably_emulator"
  const val DEVICE_ORIENTATION = "device_orientation"
}

object AnalyticsLabels{
  const val INSTALL_ACTION = "wallet_install_action"
  const val OPEN_INTENT_ACTION = "open_intent_action"
}

object AnalyticsEvents {
  const val SDK_IAP_PURCHASE_INTENT_START = "sdk_iap_purchase_intent_click"
  const val SDK_OPEN_WALLET_ATTEMPT = "sdk_iap_open_wallet_attempt"
  const val SDK_WALLET_INSTALL_IMPRESSION = "sdk_wallet_install_impression"
  const val SDK_WALLET_INSTALL_CLICK = "sdk_wallet_install_click"
  const val SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION = "sdk_download_wallet_vanilla_impression"
  const val SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION = "sdk_download_wallet_fallback_impression"
  const val SDK_INSTALL_WALLET_FEEDBACK = "sdk_install_wallet_feedback"
}

object PayAsAGuestEvents {
}