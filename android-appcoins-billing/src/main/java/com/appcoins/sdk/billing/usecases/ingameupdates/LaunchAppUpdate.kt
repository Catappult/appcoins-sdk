package com.appcoins.sdk.billing.usecases.ingameupdates

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.managers.StoreLinkMapperManager
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

object LaunchAppUpdate : UseCase() {

    operator fun invoke(context: Context) {
        super.invokeUseCase()
        logInfo("LaunchAppUpdate")
        val storeDeeplink = StoreLinkMapperManager(context).getStoreDeepLink()

        val storeLinkMethods = storeDeeplink?.storeLinkMethods
        if (storeLinkMethods.isNullOrEmpty()) {
            // Launch deeplink with default Store Deeplink
            launchDeeplink(context, getDefaultStoreDeepLink(context))
        } else {
            storeLinkMethods.forEach {
                val deeplinkLaunchedSuccessfully = launchDeeplink(context, it.deeplink)
                if (deeplinkLaunchedSuccessfully) {
                    return
                }
            }
            // Launch deeplink with default Store Deeplink
            launchDeeplink(context, getDefaultStoreDeepLink(context))
        }
    }

    private fun launchDeeplink(context: Context, deeplink: String): Boolean {
        val deeplinkIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(deeplink))
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
        return try {
            context.startActivity(deeplinkIntent)
            WalletUtils.sdkAnalytics.sendLaunchAppUpdateResultEvent(deeplink)
            true
        } catch (e: ActivityNotFoundException) {
            logError("Failed to launch App Update Deeplink: $e")
            false
        }
    }

    private fun getDefaultStoreDeepLink(context: Context) =
        GetVanillaDeepLink(context.packageName)
            .takeIf { IsAppInstalled(context, BuildConfig.APTOIDE_PACKAGE_NAME) }
            ?: GetDefaultMarketDeepLink(context.packageName)
}
