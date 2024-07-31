package com.appcoins.sdk.billing.webpayment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.appcoins.billing.sdk.R


class WebPaymentActivity : Activity() {

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(URL)

        if (url == null) {
            //send error message
            finish()
            return
        }

        setContentView(R.layout.web_payment_activity)

        val webView = findViewById<WebView>(R.id.web_view)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }

        webView.addJavascriptInterface(WebPaymentSDKInterface(), "WebPaymentSDKInterface")
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
    }

    companion object {
        private const val URL = "URL"

        @JvmStatic
        fun newIntent(context: Context, url: String): Intent {
            val intent = Intent(context, WebPaymentActivity::class.java)
            intent.putExtra(URL, url)
            return intent
        }
    }
}
