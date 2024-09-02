package com.appcoins.sdk.billing.webpayment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.helpers.AppDetailsHelper
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.Companion.DEFAULT_PAYMENT_FLOW
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.ui.RoundedImageView
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

        val sku = intent.getStringExtra(SKU)
        WalletUtils.getSdkAnalytics().sendPurchaseViaWebEvent(sku ?: "")

        setupHeader()
        setupBackgroundToClose()
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
                logError("There was a failure receiving the purchase result from the WebView", e)
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

    private fun setupHeader() {
        val paymentFlow = intent?.getStringExtra(PAYMENT_FLOW)
        val isWalletTypePayment =
            (paymentFlow == null || intent?.getStringExtra(PAYMENT_FLOW) == DEFAULT_PAYMENT_FLOW)
        if (isWalletTypePayment) {
            setupWalletTypeHeader()
        } else {
            setupExternalTypeHeader()
        }
    }

    private fun setupWalletTypeHeader() {
        findViewById<ImageView>(R.id.wallet_logo).apply { visibility = View.VISIBLE }
        findViewById<RoundedImageView>(R.id.application_logo).apply { visibility = View.GONE }
        findViewById<TextView>(R.id.application_title).apply { visibility = View.GONE }
    }

    private fun setupExternalTypeHeader() {
        val appDetailsHelper = AppDetailsHelper()
        findViewById<ImageView>(R.id.wallet_logo).apply {
            visibility = View.GONE
        }
        findViewById<RoundedImageView>(R.id.application_logo).apply {
            appDetailsHelper.getAppLauncherIcon(WalletUtils.getContext()).let { appLauncher ->
                visibility = View.VISIBLE
                setImageDrawable(appLauncher)
            }
        }
        findViewById<TextView>(R.id.application_title).apply {
            appDetailsHelper.getAppName(WalletUtils.getContext()).let { appTitle ->
                visibility = View.VISIBLE
                text = appTitle
            }
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

        mConstraintSet.constrainPercentHeight(R.id.container_for_web_view, 0.6f)
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
        private const val SKU = "SKU"
        private const val PAYMENT_FLOW = "PAYMENT_FLOW"

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
            return intent
        }
    }
}
