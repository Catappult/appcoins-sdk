package com.appcoins.sdk.billing.webpayment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
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
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.billing.usecases.HandleDeeplinkFromWebView
import com.appcoins.sdk.billing.webpayment.WebViewLandscapeUtils.applyDefaultLandscapeConstraints
import com.appcoins.sdk.billing.webpayment.WebViewLandscapeUtils.applyDynamicLandscapeConstraints
import com.appcoins.sdk.billing.webpayment.WebViewOrientationUtils.setupOrientation
import com.appcoins.sdk.billing.webpayment.WebViewPortraitUtils.applyDefaultPortraitConstraints
import com.appcoins.sdk.billing.webpayment.WebViewPortraitUtils.applyDynamicPortraitConstraints
import com.appcoins.sdk.billing.webpayment.WebViewTabletUtils.applyTabletConstraints
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.ui.getScreenOrientation
import org.json.JSONObject

class WebPaymentActivity :
    Activity(),
    SDKWebPaymentInterface,
    WalletPaymentDeeplinkResponseStream.Consumer<SDKWebResponse> {

    private var webView: WebView? = null
    private var webViewContainer: LinearLayout? = null
    private var baseConstraintLayout: ConstraintLayout? = null

    private var responseReceived = false

    private var walletDeeplinkResponseCode: Int? = null

    private var skuType: String? = null

    private var webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails? = null

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

        skuType = intent.getStringExtra(SKU_TYPE)
        val sku = intent.getStringExtra(SKU)
        WalletUtils.getSdkAnalytics().sendPurchaseViaWebEvent(sku ?: "")

        webViewDetails =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(WEB_VIEW_DETAILS, PaymentFlowMethod.WebPayment.WebViewDetails::class.java)
            } else {
                intent.getSerializableExtra(WEB_VIEW_DETAILS) as PaymentFlowMethod.WebPayment.WebViewDetails?
            }

        setupOrientation(this, webViewDetails)
        setupBackgroundToClose()
        setupWebView(url)
        adjustWebViewSize(getScreenOrientation(this))
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
                logInfo(
                    "Received Payment Result with " +
                        "responseCode: ${sdkWebResponse.responseCode} " +
                        "for sku: ${sdkWebResponse.purchaseData?.productId}"
                )
                val paymentResponse = sdkWebResponse.toSDKPaymentResponse(skuType)
                logInfo("Sending Payment Result with resultCode: ${paymentResponse.resultCode}")
                PaymentResponseStream.getInstance().emit(paymentResponse)
            } catch (e: Exception) {
                logError("There was a failure receiving the purchase result from the WebView.", e)
                WalletUtils.getSdkAnalytics().sendUnsuccessfulWebViewResultEvent(e.toString())
                PaymentResponseStream.getInstance().emit(SDKPaymentResponse.createErrorTypeResponse())
            }
        } ?: PaymentResponseStream.getInstance().emit(SDKPaymentResponse.createErrorTypeResponse())
    }

    @JavascriptInterface
    override fun openDeeplink(url: String): Boolean {
        return HandleDeeplinkFromWebView(url, this)
    }

    override fun onDestroy() {
        if (!responseReceived) {
            val sdkPaymentResponse =
                walletDeeplinkResponseCode?.let { SDKWebResponse(it).toSDKPaymentResponse() }
                    ?: SDKPaymentResponse.createCanceledTypeResponse()
            PaymentResponseStream.getInstance().emit(sdkPaymentResponse)
        }
        removeWalletPurchaseResultDeeplinkStreamCollector()
        super.onDestroy()
    }

    override fun accept(value: SDKWebResponse) {
        logInfo("Received response from WalletPaymentDeeplinkResponseStream with responseCode: ${value.responseCode}.")
        if (value.responseCode == ResponseCode.OK.value) {
            logInfo(
                "Response code successful. " +
                    "Sending Purchase Result and finishing WebPaymentActivity."
            )
            responseReceived = true
            PaymentResponseStream.getInstance().emit(value.toSDKPaymentResponse(skuType))
            finish()
            return
        }
        walletDeeplinkResponseCode = value.responseCode
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
                    orientation == Configuration.ORIENTATION_LANDSCAPE &&
                        webViewDetails?.hasLandscapeDetails() ?: false ->
                        applyDynamicLandscapeConstraints(
                            this,
                            mBaseConstraintLayout,
                            webViewContainerParams,
                            webViewDetails!!.landscapeScreenDimensions
                        )

                    orientation == Configuration.ORIENTATION_PORTRAIT &&
                        webViewDetails?.hasPortraitDetails() ?: false ->
                        applyDynamicPortraitConstraints(
                            this,
                            mBaseConstraintLayout,
                            webViewContainerParams,
                            webViewDetails!!.portraitScreenDimensions
                        )

                    resources.getBoolean(R.bool.isTablet) ->
                        applyTabletConstraints(this, mBaseConstraintLayout, webViewContainerParams)

                    orientation == Configuration.ORIENTATION_LANDSCAPE ->
                        applyDefaultLandscapeConstraints(mBaseConstraintLayout, webViewContainerParams)

                    else -> applyDefaultPortraitConstraints(this, mBaseConstraintLayout, webViewContainerParams)
                }

                webViewContainer?.layoutParams = webViewContainerParams
            }
        }
    }

    private fun observeWalletPurchaseResultDeeplinkStream() {
        WalletPaymentDeeplinkResponseStream.getInstance().collect(this)
    }

    private fun removeWalletPurchaseResultDeeplinkStreamCollector() {
        WalletPaymentDeeplinkResponseStream.getInstance().removeCollector(this)
    }

    companion object {
        private const val URL = "URL"
        private const val SKU = "SKU"
        private const val SKU_TYPE = "SKU_TYPE"
        private const val WEB_VIEW_DETAILS = "WEB_VIEW_DETAILS"

        @JvmStatic
        fun newIntent(
            context: Context,
            url: String,
            sku: String,
            skuType: String,
            webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails?
        ): Intent {
            val intent = Intent(context, WebPaymentActivity::class.java)
            intent.putExtra(URL, url)
            intent.putExtra(SKU, sku)
            intent.putExtra(SKU_TYPE, skuType)
            webViewDetails?.let { intent.putExtra(WEB_VIEW_DETAILS, it) }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return intent
        }
    }
}
