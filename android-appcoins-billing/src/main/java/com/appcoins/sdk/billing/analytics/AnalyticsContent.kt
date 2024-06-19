package com.appcoins.sdk.billing.analytics

object AnalyticsSuperLabels {
  const val INSTANCE_ID = "instance_id"
  const val SDK_VERSION_CODE = "version_code"
  const val GAME_PACKAGE_NAME = "package_name"
  const val SDK_PACKAGE = "sdk_package"

  const val OS_VERSION = "os_version"
  const val BRAND = "device_brand"
  const val MODEL = "device_model"
  const val LANGUAGE = "language"
  const val IS_EMULATOR = "probably_emulator"
}

object AnalyticsLabels {
  const val SKU_NAME = "sku_name"
  const val INSTALL_ACTION = "wallet_install_action"
  const val STORE_TYPE = "store_type"
  const val BACKEND_RESPONSE_CODE = "response_code"
  const val BACKEND_RESPONSE_MESSAGE = "response_message"
  const val BIND_SERVICE_METHOD = "bind_service"
  const val BIND_SERVICE_PRIORITY = "priority"
  const val PAYMENT_STATUS = "status"
  const val PAYMENT_STATUS_MESSAGE = "status_message"
  const val FAILURE_TYPE = "failure_type"
}

object SdkAnalyticsEvents {
  const val SDK_START_CONNECTION = "sdk_start_connection"
  const val SDK_IAP_PURCHASE_INTENT_START = "sdk_iap_purchase_intent_click"
  const val SDK_IAP_PAYMENT_STATUS_FEEDBACK = "sdk_iap_payment_status_feedback"
  const val SDK_WEB_PAYMENT_IMPRESSION = "sdk_web_payment_impression"
  const val SDK_UNEXPECTED_FAILURE = "sdk_unexpected_failure"
}

object SdkInstallFlowEvents {
  const val SDK_WALLET_INSTALL_IMPRESSION = "sdk_wallet_install_impression"
  const val SDK_WALLET_INSTALL_CLICK = "sdk_wallet_install_click"
  const val SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION = "sdk_download_wallet_vanilla_impression"
  const val SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION = "sdk_download_wallet_fallback_impression"
  const val SDK_INSTALL_WALLET_FEEDBACK = "sdk_install_wallet_feedback"
}

object SdkBackendPayflowEvents {
  const val SDK_CALL_BACKEND_PAYFLOW = "sdk_call_backend_payflow_response"
  const val SDK_CALL_BACKEND_WEB_PAYMENT_URL = "sdk_call_backend_web_payment_url"
  const val SDK_CALL_BACKEND_ATTRIBUTION = "sdk_call_backend_attribution_response"
  const val SDK_CALL_BACKEND_APP_VERSION = "sdk_call_backend_app_version_response"
  const val SDK_CALL_BACKEND_STORE_LINK = "sdk_call_backend_store_link_response"
  const val SDK_CALL_BINDSERVICE_ATTEMPT = "sdk_call_bindservice_attempt"
  const val SDK_CALL_BINDSERVICE_FAIL = "sdk_call_bindservice_fail"
}

object SdkAnalyticsFailureLabels {
  const val SDK_WEB_PAYMENT_URL_GENERATION_FAILED = "sdk_web_payment_url_generation_failed"
  const val SDK_BACKEND_GUEST_UID_GENERATION_FAILED = "sdk_backend_guest_uid_generation_failed"
}

object PayAsAGuestEvents {
}