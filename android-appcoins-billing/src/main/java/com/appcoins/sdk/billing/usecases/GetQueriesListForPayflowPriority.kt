package com.appcoins.sdk.billing.usecases

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.UserCountryUtils
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences

object GetQueriesListForPayflowPriority : UseCase() {
    operator fun invoke(): MutableMap<String, String> {
        super.invokeUseCase()
        val integratedGameVersionCode =
            GetAppInstalledVersion(WalletUtils.context.packageName, WalletUtils.context)

        val walletVersionCode =
            GetAppInstalledVersion(
                BuildConfig.APPCOINS_WALLET_PACKAGE_NAME,
                WalletUtils.context
            )
        val gamesHubVersionCode = handleGamesHubPackage()
        val aptoideGamesVersionCode =
            GetAppInstalledVersion(
                BuildConfig.APTOIDE_GAMES_PACKAGE_NAME,
                WalletUtils.context
            )
        val vanillaVersionCode =
            GetAppInstalledVersion(BuildConfig.APTOIDE_PACKAGE_NAME, WalletUtils.context)

        val attributionSharedPreferences = AttributionSharedPreferences(WalletUtils.context)
        val oemId =
            GetOemIdForPackage(WalletUtils.context.packageName, WalletUtils.context)
        val walletId = attributionSharedPreferences.getWalletId()

        val queries: MutableMap<String, String> = LinkedHashMap()

        queries["package"] = WalletUtils.context.packageName
        queries["package_vercode"] = integratedGameVersionCode.toString()
        queries["sdk_vercode"] = BuildConfig.VERSION_CODE.toString()
        walletVersionCode.let { if (it != -1) queries["wallet_vercode"] = it.toString() }
        gamesHubVersionCode.let { if (it != -1) queries["gh_vercode"] = it.toString() }
        aptoideGamesVersionCode.let {
            if (it != -1) queries["aptoide_games_vercode"] = it.toString()
        }
        vanillaVersionCode.let { if (it != -1) queries["vanilla_vercode"] = it.toString() }
        UserCountryUtils.getUserCountry(WalletUtils.context)?.let { queries["locale"] = it }
        oemId?.let { queries["oemid"] = it }
        walletId?.let { queries["guest_id"] = it }

        return queries
    }

    /**
     * Currently in dev environment, the GamesHub can have two different packages installed
     */
    private fun handleGamesHubPackage(): Int {
        val version =
            GetAppInstalledVersion(
                BuildConfig.GAMESHUB_PACKAGE_NAME,
                WalletUtils.context
            )
        return if (BuildConfig.DEBUG && version == -1) {
            GetAppInstalledVersion(
                BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE,
                WalletUtils.context
            )
        } else {
            version
        }
    }
}
