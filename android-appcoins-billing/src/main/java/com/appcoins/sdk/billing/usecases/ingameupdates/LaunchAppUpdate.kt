package com.appcoins.sdk.billing.usecases.ingameupdates

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.managers.StoreDeepLinkManager
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.logger.Logger.logInfo

object LaunchAppUpdate : UseCase() {

    operator fun invoke(context: Context) {
        super.invokeUseCase()
        logInfo("LaunchAppUpdate")
        val storeDeeplink = StoreDeepLinkManager(context).getStoreDeepLink()
        launchDeeplink(context, storeDeeplink)
    }

    private fun launchDeeplink(context: Context, deeplink: String? = null) {
        val uriDeeplink = deeplink
            ?: GetVanillaDeepLink(context.packageName)
                .takeIf { IsAppInstalled(context, BuildConfig.APTOIDE_PACKAGE_NAME) }
            ?: GetDefaultMarketDeepLink(context.packageName)

        WalletUtils.getSdkAnalytics().appUpdateDeeplinkImpression(uriDeeplink)

        val deeplinkIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(uriDeeplink))
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
        try {
            context.startActivity(deeplinkIntent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}
