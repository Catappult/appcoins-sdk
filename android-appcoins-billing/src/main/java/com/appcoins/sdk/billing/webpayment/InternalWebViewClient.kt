package com.appcoins.sdk.billing.webpayment

import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.CATEGORY_BROWSABLE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo


internal class InternalWebViewClient(private val activity: Activity) : WebViewClient() {

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
        try {
            handleUri(Uri.parse(url))
        } catch (e: Exception) {
            logError("There was a failure with the URL to Override.", e)
            false
        }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean =
        handleUri(request.url)

    private fun handleUri(uri: Uri): Boolean {
        try {
            logDebug("$uri")
            logInfo(uri.scheme.toString())

            if (canHandleWebDeeplinkScheme(uri)) return true
            if (canHandleApplicationDeeplink(uri)) return true
        } catch (e: Exception) {
            logError("There was a failure with the URL to Override.", e)
        }
        return false
    }

    private fun canHandleWebDeeplinkScheme(uri: Uri): Boolean =
        if (uri.scheme.equals(WEB_DEEPLINK_SCHEME)) {
            activity.finish()
            true
        } else {
            false
        }

    private fun canHandleApplicationDeeplink(uri: Uri): Boolean =
        try {
            val intent = Intent(ACTION_VIEW, uri).apply {
                addCategory(CATEGORY_BROWSABLE)
                flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                } else {
                    FLAG_ACTIVITY_NEW_TASK
                }
            }
            activity.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }


    private companion object {
        const val WEB_DEEPLINK_SCHEME = "web-iap-result"
    }
}
