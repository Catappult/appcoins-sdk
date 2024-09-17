package com.appcoins.sdk.billing.webpayment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.ui.floatToDps
import com.appcoins.sdk.core.ui.getScreenHeightInDp
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
            logError("URL not present in the Bundle. Aborting the WebView Payment.")
            PaymentResponseStream.getInstance().emit(SDKPaymentResponse.createErrorTypeResponse())
            finish()
            return
        }

        connectViews()

        if (savedInstanceState != null) {
            webView?.restoreState(savedInstanceState)
            return
        }

        val sku = intent.getStringExtra(SKU)
        WalletUtils.getSdkAnalytics().sendPurchaseViaWebEvent(sku ?: "")

        setupBackgroundToClose()
        setupWebView(url)
        adjustWebViewSize(resources.configuration.orientation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustWebViewSize(newConfig.orientation)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.saveState(outState)
    }

    @JavascriptInterface
    override fun onPurchaseResult(result: String?) {
        responseReceived = true
        logDebug(result ?: "")
        logInfo("Received response from WebView Payment Result.")
        result?.apply {
            try {
                val jsonObject = JSONObject(this)
                val sdkWebResponse = SDKWebResponse(jsonObject)
                logInfo("Received Payment Result with responseCode: ${sdkWebResponse.responseCode} for sku: ${sdkWebResponse.purchaseData?.productId}")
                val paymentResponse = sdkWebResponse.toSDKPaymentResponse()
                logInfo("Sending Payment Result with resultCode: ${paymentResponse.resultCode}")
                PaymentResponseStream.getInstance().emit(paymentResponse)
            } catch (e: Exception) {
                logError("There was a failure receiving the purchase result from the WebView.", e)
                WalletUtils.getSdkAnalytics().sendUnsuccessfulWebViewResultEvent(e.toString())
                PaymentResponseStream.getInstance()
                    .emit(SDKPaymentResponse.createErrorTypeResponse())
            }
        } ?: PaymentResponseStream.getInstance().emit(SDKPaymentResponse.createErrorTypeResponse())
    }

    @JavascriptInterface
    override fun onClose() {
        finish()
    }

    override fun onDestroy() {
        if (!responseReceived) {
            PaymentResponseStream.getInstance()
                .emit(SDKPaymentResponse.createCanceledTypeResponse())
        }
        super.onDestroy()
    }

    private fun connectViews() {
        webView = findViewById(R.id.web_view)
        webViewContainer = findViewById(R.id.container_for_web_view)
        baseConstraintLayout = findViewById(R.id.base_constraint_layout)
    }

    private fun setupBackgroundToClose() {
        findViewById<ConstraintLayout>(R.id.base_constraint_layout).setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(url: String) {
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.databaseEnabled = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }

        webView?.addJavascriptInterface(this as SDKWebPaymentInterface, "SDKWebPaymentInterface")
        webView?.webViewClient = InternalWebViewClient(this)
        logDebug("Loading WebView for URL: $url")
        logInfo("Loading WebView to start Web Payment.")
        webView?.loadUrl(url)
    }

    private fun adjustWebViewSize(orientation: Int) {
        val mWebViewContainer = webViewContainer
        val mBaseConstraintLayout = baseConstraintLayout

        if (mWebViewContainer != null && mBaseConstraintLayout != null) {
            val webViewContainerParams = mWebViewContainer.layoutParams

            if (webViewContainerParams != null) {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE
                    || resources.getBoolean(R.bool.isTablet)
                ) {
                    webViewContainerParams.width = 0
                    webViewContainerParams.height = 0

                    applyLandscapeConstraints(mBaseConstraintLayout)
                } else {
                    webViewContainerParams.width = 0
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

        mConstraintSet.constrainPercentHeight(
            R.id.container_for_web_view,
            LANDSCAPE_MAX_HEIGHT_PERCENT
        )
        mConstraintSet.constrainPercentWidth(
            R.id.container_for_web_view,
            LANDSCAPE_MAX_WIDTH_PERCENT
        )
        mConstraintSet.constrainMaxHeight(
            R.id.container_for_web_view,
            floatToDps(LANDSCAPE_MAX_HEIGHT_PIXELS, this).toInt()
        )
        mConstraintSet.constrainMaxWidth(
            R.id.container_for_web_view,
            floatToDps(LANDSCAPE_MAX_WIDTH_PIXELS, this).toInt()
        )

        mConstraintSet.applyTo(mBaseConstraintLayout)
    }

    private fun applyPortraitConstraints(mBaseConstraintLayout: ConstraintLayout) {
        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        logInfo("getScreenHeightInDp: ${getScreenHeightInDp(this)}")

        val maxHeightPercentage =
            if (getScreenHeightInDp(this) < SMALL_PHONE_HEIGHT)
                SMALL_PHONE_PORTRAIT_MAX_HEIGHT_PERCENT
            else NORMAL_PHONE_PORTRAIT_MAX_HEIGHT_PERCENT

        logInfo("maxHeightPercentage: $maxHeightPercentage")
        mConstraintSet.constrainPercentHeight(R.id.container_for_web_view, maxHeightPercentage)
        mConstraintSet.constrainPercentWidth(R.id.container_for_web_view, PORTRAIT_WIDTH_PERCENT)
        mConstraintSet.constrainMaxHeight(
            R.id.container_for_web_view,
            floatToDps(PORTRAIT_MAX_HEIGHT_PIXELS, this).toInt()
        )
        mConstraintSet.constrainMaxWidth(R.id.container_for_web_view, 0)

        mConstraintSet.applyTo(mBaseConstraintLayout)
    }

    companion object {
        private const val URL = "URL"
        private const val SKU = "SKU"
        private const val PAYMENT_FLOW = "PAYMENT_FLOW"

        private const val SMALL_PHONE_HEIGHT = 650

        // Landscape Constants
        private const val LANDSCAPE_MAX_HEIGHT_PIXELS = 336f
        private const val LANDSCAPE_MAX_WIDTH_PIXELS = 688f
        private const val LANDSCAPE_MAX_HEIGHT_PERCENT = 0.9f
        private const val LANDSCAPE_MAX_WIDTH_PERCENT = 0.9f

        // Portrait Constants
        private const val PORTRAIT_MAX_HEIGHT_PIXELS = 504f
        private const val SMALL_PHONE_PORTRAIT_MAX_HEIGHT_PERCENT = 0.8f
        private const val NORMAL_PHONE_PORTRAIT_MAX_HEIGHT_PERCENT = 0.6f
        private const val PORTRAIT_WIDTH_PERCENT = 1f

        @JvmStatic
        fun newIntent(
            context: Context,
            url: String,
            sku: String,
            paymentFlow: String?
        ): Intent {
            val intent = Intent(context, WebPaymentActivity::class.java)
            intent.putExtra(URL, url)
            intent.putExtra(SKU, sku)
            paymentFlow?.let { intent.putExtra(PAYMENT_FLOW, it) }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return intent
        }
    }
}
