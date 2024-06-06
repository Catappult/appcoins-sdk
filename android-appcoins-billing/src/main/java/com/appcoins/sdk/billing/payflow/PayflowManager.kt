package com.appcoins.sdk.billing.payflow

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.helpers.UserCountryUtils.getUserCountry
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.helpers.WalletUtils.getAppInstalledVersion
import com.appcoins.sdk.billing.helpers.WalletUtils.setPayflowMethodsList
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.usecases.GetOemIdForPackage
import com.appcoins.sdk.billing.utils.ServiceUtils

class PayflowManager(val packageName: String) {
  private val payflowRepository = PayflowRepository(BdsService(BuildConfig.PAYFLOW_HOST, 30000))

  fun getPayflowPriority(billingFlowParams: BillingFlowParams?) {
    val integratedGameVersionCode = getAppInstalledVersion(packageName)
    val walletVersionCode = getAppInstalledVersion(BuildConfig.APPCOINS_WALLET_PACKAGE_NAME)
    val gamesHubVersionCode = handleGamesHubPackage()
    val vanillaVersionCode = getAppInstalledVersion(BuildConfig.APTOIDE_PACKAGE_NAME)

    val attributionSharedPreferences = AttributionSharedPreferences(WalletUtils.context)
    val oemId =
      GetOemIdForPackage.invoke(WalletUtils.context.packageName, WalletUtils.context)
    val walletId = attributionSharedPreferences.getWalletId()

    val paymentFlowMethodList =
      payflowRepository.getPayflowPriority(
        packageName,
        integratedGameVersionCode,
        BuildConfig.VERSION_CODE,
        if (walletVersionCode == -1) null else walletVersionCode,
        if (gamesHubVersionCode == -1) null else gamesHubVersionCode,
        if (vanillaVersionCode == -1) null else vanillaVersionCode,
        getUserCountry(WalletUtils.context),
        oemId,
        walletId,
        billingFlowParams
      )
    setPayflowMethodsList(paymentFlowMethodList)
  }

  fun getPayflowPriorityAsync(billingFlowParams: BillingFlowParams?) {
    val payflowListener = object : PayflowListener {
      override fun onResponse(payflowMethodResponse: PayflowMethodResponse) {
        payflowMethodResponse.responseCode?.let { responseCode ->
          val sortedMethods = payflowMethodResponse.paymentFlowList?.sortedBy { it.priority }
          if (ServiceUtils.isSuccess(responseCode)) {
            setPayflowMethodsList(sortedMethods)
          } else {
            setPayflowMethodsList(null)
          }
        }
      }
    }

    val integratedGameVersionCode = getAppInstalledVersion(packageName)
    val walletVersionCode = getAppInstalledVersion(BuildConfig.APPCOINS_WALLET_PACKAGE_NAME)
    val gamesHubVersionCode = handleGamesHubPackage()
    val vanillaVersionCode = getAppInstalledVersion(BuildConfig.APTOIDE_PACKAGE_NAME)

    val attributionSharedPreferences = AttributionSharedPreferences(WalletUtils.context)
    val oemId =
      GetOemIdForPackage.invoke(WalletUtils.context.packageName, WalletUtils.context)
    val walletId = attributionSharedPreferences.getWalletId()

    payflowRepository.getPayflowPriorityAsync(
      payflowListener,
      packageName,
      integratedGameVersionCode,
      BuildConfig.VERSION_CODE,
      if (walletVersionCode == -1) null else walletVersionCode,
      if (gamesHubVersionCode == -1) null else gamesHubVersionCode,
      if (vanillaVersionCode == -1) null else vanillaVersionCode,
      getUserCountry(WalletUtils.context),
      oemId,
      walletId,
      billingFlowParams
    )
  }

  /**
   * Currently in dev environment, the GamesHub can have two different packages installed
   */
  private fun handleGamesHubPackage() : Int {
    val version = getAppInstalledVersion(BuildConfig.GAMESHUB_PACKAGE_NAME)
    return if (BuildConfig.DEBUG && version == -1) {
      getAppInstalledVersion(BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE)
    } else {
      version
    }
  }
}
