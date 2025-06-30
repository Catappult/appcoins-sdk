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
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.billing.listeners.SDKWebResponse
import com.appcoins.sdk.billing.listeners.WalletPaymentDeeplinkResponseStream
import com.appcoins.sdk.billing.listeners.WebPaymentActionStream
import com.appcoins.sdk.billing.payflow.models.WebViewDetails
import com.appcoins.sdk.billing.usecases.HandleDeeplinkFromWebView
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESULT_CODE
import com.appcoins.sdk.billing.webpayment.WebViewLandscapeUtils.applyDefaultLandscapeConstraints
import com.appcoins.sdk.billing.webpayment.WebViewLandscapeUtils.applyDynamicLandscapeConstraints
import com.appcoins.sdk.billing.webpayment.WebViewOrientationUtils.setupOrientation
import com.appcoins.sdk.billing.webpayment.WebViewPortraitUtils.applyDefaultPortraitConstraints
import com.appcoins.sdk.billing.webpayment.WebViewPortraitUtils.applyDynamicPortraitConstraints
import com.appcoins.sdk.billing.webpayment.WebViewTabletUtils.applyTabletConstraints
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.ui.getScreenOrientation
import org.json.JSONObject

class WebPaymentActivity :
    Activity(),
    SDKWebPaymentInterface,
    WalletPaymentDeeplinkResponseStream.Consumer<SDKWebResponse>,
    WebPaymentActionStream.Consumer<String> {

    private var webView: WebView? = null
    private val internalWebViewClient: InternalWebViewClient by lazy { InternalWebViewClient(this) }

    private var webViewContainer: LinearLayout? = null
    private var baseConstraintLayout: ConstraintLayout? = null

    private var responseReceived = false

    private var walletDeeplinkResponseCode: Int? = null

    private var skuType: String? = null

    private var webViewDetails: WebViewDetails? = null

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
        SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentStartEvent(url)

        webViewDetails =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(WEB_VIEW_DETAILS, WebViewDetails::class.java)
            } else {
                intent.getSerializableExtra(WEB_VIEW_DETAILS) as WebViewDetails?
            }

        setupOrientation(this, webViewDetails)
        setupBackgroundToClose()
        setupWebView(url)
        adjustWebViewSize(getScreenOrientation(this))
        observeWalletPurchaseResultDeeplinkStream()
        observeWebPaymentActionStream()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustWebViewSize(newConfig.orientation)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.saveState(outState)
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

    // WalletPaymentDeeplinkResponseStream
    override fun accept(value: SDKWebResponse) {
        logInfo("Received response from WalletPaymentDeeplinkResponseStream with responseCode: ${value.responseCode}.")
        SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentWalletPaymentResultEvent()
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

    // WebPaymentActionStream
    override fun acceptWebPaymentActionStream(value: String?) {
        notifyWebViewOfExternalPaymentResult(value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logInfo(
            "Received response from External Payment Activity.\nRequest Code: $requestCode\nResult Code: $resultCode"
        )
        logDebug("Extras: " + data?.extras)
        if (requestCode == RESULT_CODE) {
            notifyWebViewOfExternalPaymentResult()
        }
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            logInfo("Going back in WebView.")
            webView?.goBack()
        } else {
            logInfo("WebView already at initial page. Exiting activity.")
            super.onBackPressed()
        }
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
                SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentErrorProcessingPurchaseResultEvent(e.toString())
                PaymentResponseStream.getInstance().emit(SDKPaymentResponse.createErrorTypeResponse())
            }
        } ?: run {
            SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentPurchaseResultEmptyEvent()
            PaymentResponseStream.getInstance().emit(SDKPaymentResponse.createErrorTypeResponse())
        }
    }

    @JavascriptInterface
    override fun openDeeplink(url: String): Boolean {
        return HandleDeeplinkFromWebView(url, this)
    }

    @JavascriptInterface
    override fun startExternalPayment(url: String): Boolean {
        SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentLaunchExternalPaymentEvent(url)
        startActivityForResult(ExternalPaymentActivity.newIntent(this, url), RESULT_CODE)
        return true
    }

    @JavascriptInterface
    override fun allowExternalApps(allow: Boolean) {
        SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentAllowExternalAppsEvent(allow)
        internalWebViewClient.shouldAllowExternalApps = allow
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
        webView?.webViewClient = internalWebViewClient
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

    private fun observeWebPaymentActionStream() {
        WebPaymentActionStream.getInstance().collect(this)
    }

    private fun notifyWebViewOfExternalPaymentResult(data: String? = null) {
        if (data != null) {
            SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentExecuteExternalDeeplinkEvent(data)
            webView?.loadUrl("javascript:onPaymentStateUpdated(\"${data}\")")
        } else {
            SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentExternalPaymentResultEvent()
            webView?.loadUrl("javascript:onPaymentStateUpdated()")
        }
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
            webViewDetails: WebViewDetails?
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
