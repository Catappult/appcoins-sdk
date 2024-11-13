package com.appcoins.sdk.billing.webpayment

import android.annotation.TargetApi
import android.app.Activity
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
        } catch (e: Exception) {
            logError("There was a failure with the URL to Override.", e)
        }
        logInfo("SDK can't handle internally the Deeplink. WebView should handle.")
        return false
    }

    private fun canHandleWebDeeplinkScheme(uri: Uri): Boolean =
        if (uri.scheme.equals(WEB_DEEPLINK_SCHEME)) {
            logInfo("Handling WebDeeplinkScheme.")
            activity.finish()
            true
        } else {
            false
        }

    private companion object {
        const val WEB_DEEPLINK_SCHEME = "web-iap-result"
    }
}
