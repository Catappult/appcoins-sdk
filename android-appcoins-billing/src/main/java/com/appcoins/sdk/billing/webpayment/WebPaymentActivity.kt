package com.appcoins.sdk.billing.webpayment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.core.logger.Logger.logInfo
import org.json.JSONObject

class WebPaymentActivity : Activity(), SDKWebPaymentInterface {

    private var webView: WebView? = null
    private var webViewContainer: LinearLayout? = null
    private var baseConstraintLayout: ConstraintLayout? = null

    private var responseReceived = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_payment_activity)

        val url = intent.getStringExtra(URL)

        if (url == null) {
            PaymentResponseStream.getInstance()
                .emit(SDKWebResponse(ResponseCode.ERROR.value).toSDKPaymentResponse())
            finish()
            return
        }

        connectViews()

        if (savedInstanceState != null) {
            webView?.restoreState(savedInstanceState)
            return
        }

        setupCloseButton()
        setupWebView(url)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustWebViewSize(newConfig.orientation)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.saveState(outState)
    }

    override fun onPurchaseResult(result: String?) {
        responseReceived = true
        logInfo(result ?: "")
        result?.apply {
            val jsonObject = JSONObject(this)
            PaymentResponseStream.getInstance()
                .emit(SDKWebResponse(jsonObject).toSDKPaymentResponse())
        } ?: PaymentResponseStream.getInstance().emit(SDKPaymentResponse(ResponseCode.ERROR.value))
    }

    override fun onClose() {
        finish()
    }

    override fun onDestroy() {
        if (!responseReceived) {
            PaymentResponseStream.getInstance()
                .emit(SDKPaymentResponse(ResponseCode.USER_CANCELED.value))
        }
        super.onDestroy()
    }

    private fun connectViews() {
        webView = findViewById(R.id.web_view)
        webViewContainer = findViewById(R.id.container_for_web_view)
        baseConstraintLayout = findViewById(R.id.base_constraint_layout)
    }

    private fun setupCloseButton() {
        findViewById<ImageView>(R.id.button_close).setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(url: String) {
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.databaseEnabled = true

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }

        webView?.addJavascriptInterface(this as SDKWebPaymentInterface, "SDKWebPaymentInterface")
        webView?.webViewClient = InternalWebViewClient(this)
        webView?.loadUrl(url)
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

                    applyLandscapeConstraints(mBaseConstraintLayout)
                } else {
                    webViewContainerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                    webViewContainerParams.height = 0

                    applyPortraitConstraints(mBaseConstraintLayout)
                }

                webViewContainer?.layoutParams = webViewContainerParams
            }
        }
    }

    private fun applyLandscapeConstraints(mBaseConstraintLayout: ConstraintLayout) {
        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        mConstraintSet.constrainPercentWidth(R.id.container_for_web_view, 0.8f)
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
    }

    private fun applyPortraitConstraints(mBaseConstraintLayout: ConstraintLayout) {
        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        mConstraintSet.constrainPercentHeight(R.id.container_for_web_view, 0.9f)
        mConstraintSet.connect(
            R.id.container_for_web_view,
            ConstraintSet.BOTTOM,
            R.id.base_constraint_layout,
            ConstraintSet.BOTTOM
        )

        mConstraintSet.applyTo(mBaseConstraintLayout)
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

    private class InternalWebViewClient(private val activity: Activity) : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            val uri = Uri.parse(url)
            logInfo(url)
            logInfo(uri.scheme.toString())
            if (uri.scheme == WEB_DEEPLINK_SCHEME) {
                activity.finish()
                return true
            }
            return false
        }

        private companion object {
            const val WEB_DEEPLINK_SCHEME = "web-iap-result"
        }
    }
}
