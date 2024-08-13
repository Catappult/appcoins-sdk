package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import com.appcoins.sdk.core.logger.Logger.logInfo

internal class InternalWebViewClient(private val activity: Activity) : WebViewClient() {
    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val uri = Uri.parse(url)
        logInfo(url ?: "")
        logInfo(uri.scheme.toString())
        if (uri.scheme.equals(WEB_DEEPLINK_SCHEME)) {
            activity.finish()
            return true
        }
        return false
    }

    private companion object {
        const val WEB_DEEPLINK_SCHEME = "web-iap-result"
    }
}
