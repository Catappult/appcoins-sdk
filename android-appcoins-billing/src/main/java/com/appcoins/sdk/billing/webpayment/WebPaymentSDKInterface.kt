package com.appcoins.sdk.billing.webpayment

import android.webkit.JavascriptInterface


class WebPaymentSDKInterface {

    @JavascriptInterface
    fun sendData(data: String?) {
        // Handle here the response from the Web Page
    }
}