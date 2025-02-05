package com.appcoins.sdk.billing.usecases

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.CATEGORY_BROWSABLE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

@Suppress("SwallowedException")
object HandleDeeplinkFromWebView : UseCase() {
    operator fun invoke(url: String, activity: Activity): Boolean {
        super.invokeUseCase()
        return try {
            SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentOpenDeeplinkEvent(url)
            val intent = Intent(ACTION_VIEW, Uri.parse(url)).apply {
                addCategory(CATEGORY_BROWSABLE)
                flags = FLAG_ACTIVITY_NEW_TASK
            }
            logInfo("Handling Application Deeplink.")
            activity.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            logError("Failed to handle Deeplink from WebView.")
            SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentFailureToOpenDeeplinkEvent(url, e.toString())
            false
        }
    }
}
