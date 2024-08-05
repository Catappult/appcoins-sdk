package com.appcoins.sdk.billing.webpayment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.appcoins.billing.sdk.R


class WebPaymentActivity : Activity() {

    private var webView: WebView? = null
    private var webViewContainer: LinearLayout? = null
    private var baseConstraintLayout: ConstraintLayout? = null

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_payment_activity)

        val url = intent.getStringExtra(URL)

        if (url == null) {
            //send error message
            finish()
            return
        }

        webView = findViewById(R.id.web_view)
        webViewContainer = findViewById(R.id.container_for_web_view)
        baseConstraintLayout = findViewById(R.id.base_constraint_layout)

        if (savedInstanceState != null) {
            webView?.restoreState(savedInstanceState)
            return
        }

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.databaseEnabled = true

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }

        webView?.addJavascriptInterface(WebPaymentSDKInterface(), "WebPaymentSDKInterface")
        webView?.webViewClient = WebViewClient()
        webView?.loadUrl(url)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustWebViewSize(newConfig.orientation)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.saveState(outState)
    }

    private fun adjustWebViewSize(orientation: Int) {
        val mWebViewContainer = webViewContainer
        val mBaseConstraintLayout = baseConstraintLayout

        if (mWebViewContainer != null && mBaseConstraintLayout != null) {
            val webViewContainerParams = mWebViewContainer.layoutParams

            if (webViewContainerParams != null) {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    webViewContainerParams.width = 0
                    webViewContainerParams.height = LinearLayout.LayoutParams.MATCH_PARENT

                    val mConstraintSet = ConstraintSet()

                    mConstraintSet.clone(baseConstraintLayout)

                    mConstraintSet.constrainPercentWidth(R.id.container_for_web_view, 0.66f)
                    mConstraintSet.connect(
                        R.id.container_for_web_view,
                        ConstraintSet.END,
                        R.id.base_constraint_layout,
                        ConstraintSet.END
                    )
                    mConstraintSet.connect(
                        R.id.container_for_web_view,
                        ConstraintSet.START,
                        R.id.base_constraint_layout,
                        ConstraintSet.START
                    )

                    mConstraintSet.applyTo(mBaseConstraintLayout)
                } else {
                    webViewContainerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                    webViewContainerParams.height = 0

                    val mConstraintSet = ConstraintSet()

                    mConstraintSet.clone(baseConstraintLayout)

                    mConstraintSet.constrainPercentHeight(R.id.container_for_web_view, 0.75f)
                    mConstraintSet.connect(
                        R.id.container_for_web_view,
                        ConstraintSet.BOTTOM,
                        R.id.base_constraint_layout,
                        ConstraintSet.BOTTOM
                    )

                    mConstraintSet.applyTo(mBaseConstraintLayout)
                }

                webViewContainer?.layoutParams = webViewContainerParams
            }
        }
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
