package com.appcoins.sdk.billing.usecases

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.UserCountryUtils
import com.appcoins.sdk.billing.helpers.WalletUtils

class GetQueriesListForPayflowPriority {
    companion object {
        fun invoke(): MutableMap<String, String> {
            val integratedGameVersionCode =
                GetAppInstalledVersion.invoke(WalletUtils.context.packageName, WalletUtils.context)
            val walletVersionCode =
                GetAppInstalledVersion.invoke(
                    BuildConfig.APPCOINS_WALLET_PACKAGE_NAME,
                    WalletUtils.context
                )
            val gamesHubVersionCode = handleGamesHubPackage()
            val vanillaVersionCode =
                GetAppInstalledVersion.invoke(BuildConfig.APTOIDE_PACKAGE_NAME, WalletUtils.context)

            val queries: MutableMap<String, String> = LinkedHashMap()

            queries["package"] = WalletUtils.context.packageName
            queries["package_vercode"] = integratedGameVersionCode.toString()
            queries["sdk_vercode"] = BuildConfig.VERSION_CODE.toString()
            walletVersionCode.let { queries["wallet_vercode"] = it.toString() }
            gamesHubVersionCode.let { queries["gh_vercode"] = it.toString() }
            vanillaVersionCode.let { queries["vanilla_vercode"] = it.toString() }
            UserCountryUtils.getUserCountry(WalletUtils.context)?.let { queries["locale"] = it }

            return queries
        }

        /**
         * Currently in dev environment, the GamesHub can have two different packages installed
         */
        private fun handleGamesHubPackage(): Int {
            val version =
                GetAppInstalledVersion.invoke(
                    BuildConfig.GAMESHUB_PACKAGE_NAME,
                    WalletUtils.context
                )
            return if (BuildConfig.DEBUG && version == -1) {
                GetAppInstalledVersion.invoke(
                    BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE,
                    WalletUtils.context
                )
            } else {
                version
            }
        }
    }
}
