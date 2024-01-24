package com.appcoins.sdk.billing.payflow

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.UserCountryUtils.getUserCountry
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.helpers.WalletUtils.getAppInstalledVersion
import com.appcoins.sdk.billing.helpers.WalletUtils.setPayflowMethodsList
import com.appcoins.sdk.billing.service.BdsService

class PayflowManager(val packageName: String) {
  private val payflowRepository = PayflowRepository(BdsService(BuildConfig.PAYFLOW_HOST, 30000))

  fun getPayflowPriority() {
    val payflowListener = object : PayflowListener {
      override fun onResponse(payflowList: List<PaymentFlowMethod>?) {
        if (!payflowList.isNullOrEmpty()) {
          val sortedMethods = payflowList.sortedBy { it.priority }
          setPayflowMethodsList(sortedMethods)
        }
      }
    }

    val integratedGameVersionCode = getAppInstalledVersion(packageName)
    val walletVersionCode = getAppInstalledVersion(BuildConfig.APPCOINS_WALLET_PACKAGE_NAME)
    val gamesHubVersionCode = handleGamesHubPackage()
    val vanillaVersionCode = getAppInstalledVersion(BuildConfig.APTOIDE_PACKAGE_NAME)

    payflowRepository.getPayflowPriority(
      payflowListener,
      packageName,
      integratedGameVersionCode,
      BuildConfig.VERSION_CODE,
      if (walletVersionCode == -1) null else walletVersionCode,
      if (gamesHubVersionCode == -1) null else gamesHubVersionCode,
      if (vanillaVersionCode == -1) null else vanillaVersionCode,
      getUserCountry(WalletUtils.context),
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
