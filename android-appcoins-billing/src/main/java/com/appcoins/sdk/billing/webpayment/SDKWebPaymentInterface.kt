package com.appcoins.sdk.billing.webpayment

import android.webkit.JavascriptInterface

interface SDKWebPaymentInterface {

    @JavascriptInterface
    fun onPurchaseResult(result: String?)

    @JavascriptInterface
    fun openDeeplink(url: String): Boolean

    @JavascriptInterface
    fun startExternalPayment(url: String): Boolean

    @JavascriptInterface
    fun allowExternalApps(allow: Boolean)
}
