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
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.billing.listeners.WalletPaymentDeeplinkResponseStream
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.ui.floatToDps
import com.appcoins.sdk.core.ui.getScreenHeightInDp
import org.json.JSONObject

class WebPaymentActivity : Activity(), SDKWebPaymentInterface,
    WalletPaymentDeeplinkResponseStream.Consumer<Int> {

    private var webView: WebView? = null
    private var webViewContainer: LinearLayout? = null
    private var baseConstraintLayout: ConstraintLayout? = null

    private var responseReceived = false

    private var walletDeeplinkResponseCode: Int? = null

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
        observeWalletPurchaseResultDeeplinkStream()
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

    override fun onDestroy() {
        if (!responseReceived) {
            val sdkPaymentResponse =
                walletDeeplinkResponseCode?.let { SDKWebResponse(it).toSDKPaymentResponse() }
                    ?: SDKPaymentResponse.createCanceledTypeResponse()
            PaymentResponseStream.getInstance().emit(sdkPaymentResponse)
        }
        super.onDestroy()
    }

    override fun accept(value: Int) {
        logInfo("Received response from WalletPaymentDeeplinkResponseStream $value.")
        if (value == ResponseCode.OK.value) {
            responseReceived = true
            logInfo("Response code successful. Finishing WebPaymentActivity.")
            finish()
            return
        }
        walletDeeplinkResponseCode = value
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
                when {
                    resources.getBoolean(R.bool.isTablet) ->
                        applyTabletConstraints(mBaseConstraintLayout, webViewContainerParams)

                    orientation == Configuration.ORIENTATION_LANDSCAPE ->
                        applyLandscapeConstraints(mBaseConstraintLayout, webViewContainerParams)

                    else -> applyPortraitConstraints(webViewContainerParams)
                }

                webViewContainer?.layoutParams = webViewContainerParams
            }
        }
    }

    private fun applyTabletConstraints(
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams
    ) {
        webViewContainerParams.width = 0
        webViewContainerParams.height = 0

        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        mConstraintSet.constrainPercentHeight(
            R.id.container_for_web_view,
            TABLET_MAX_HEIGHT_PERCENT
        )
        mConstraintSet.constrainPercentWidth(
            R.id.container_for_web_view,
            TABLET_MAX_WIDTH_PERCENT
        )
        mConstraintSet.constrainMaxHeight(
            R.id.container_for_web_view,
            floatToDps(TABLET_MAX_HEIGHT_PIXELS, this).toInt()
        )
        mConstraintSet.constrainMaxWidth(
            R.id.container_for_web_view,
            floatToDps(TABLET_MAX_WIDTH_PIXELS, this).toInt()
        )

        mConstraintSet.applyTo(mBaseConstraintLayout)
    }

    private fun applyLandscapeConstraints(
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams
    ) {
        webViewContainerParams.width = 0
        webViewContainerParams.height = 0

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
        mConstraintSet.constrainMaxHeight(R.id.container_for_web_view, 0)
        mConstraintSet.constrainMaxWidth(R.id.container_for_web_view, 0)

        mConstraintSet.applyTo(mBaseConstraintLayout)
    }

    private fun applyPortraitConstraints(webViewContainerParams: ViewGroup.LayoutParams) {
        val screenMaxHeight = getScreenHeightInDp(this)
        val heightToSet =
            if (screenMaxHeight < PORTRAIT_MAX_HEIGHT_PIXELS) LinearLayout.LayoutParams.MATCH_PARENT
            else floatToDps(PORTRAIT_MAX_HEIGHT_PIXELS, this).toInt()
        webViewContainerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        webViewContainerParams.height = heightToSet
    }

    private fun observeWalletPurchaseResultDeeplinkStream() {

    }

    companion object {
        private const val URL = "URL"
        private const val SKU = "SKU"
        private const val PAYMENT_FLOW = "PAYMENT_FLOW"

        // Tablet Constants
        private const val TABLET_MAX_HEIGHT_PIXELS = 480f
        private const val TABLET_MAX_WIDTH_PIXELS = 688f
        private const val TABLET_MAX_HEIGHT_PERCENT = 0.9f
        private const val TABLET_MAX_WIDTH_PERCENT = 0.9f

        // Landscape Constants
        private const val LANDSCAPE_MAX_HEIGHT_PERCENT = 0.9f
        private const val LANDSCAPE_MAX_WIDTH_PERCENT = 0.9f

        // Portrait Constants
        private const val PORTRAIT_MAX_HEIGHT_PIXELS = 504f

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
