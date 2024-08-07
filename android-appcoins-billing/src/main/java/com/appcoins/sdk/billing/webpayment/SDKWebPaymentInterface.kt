package com.appcoins.sdk.billing.webpayment

import android.webkit.JavascriptInterface

interface SDKWebPaymentInterface {

    @JavascriptInterface
    fun onPurchaseResult(result: String?)

    @JavascriptInterface
    fun onClose()
}