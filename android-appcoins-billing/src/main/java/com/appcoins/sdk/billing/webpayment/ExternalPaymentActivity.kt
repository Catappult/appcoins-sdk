package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.listeners.ExternalPaymentResponseStream
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

class ExternalPaymentActivity : Activity(), ExternalPaymentResponseStream.Consumer {

    private var customTabLaunched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        logInfo("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transparent_activity)

        val shouldCloseActivity = intent.getBooleanExtra(SHOULD_CLOSE_ACTIVITY, false)

        if (shouldCloseActivity) {
            finish()
            return
        }

        val url = intent.getStringExtra(URL)

        if (url == null) {
            logError("URL not present in the Bundle. Aborting External Payment Method.")
            PaymentResponseStream.getInstance().emit(SDKPaymentResponse.createErrorTypeResponse())
            finish()
            return
        }

        if (savedInstanceState != null) {
            return
        }

        observeExternalPaymentResponseStream()
        startCustomTabForExternalPayment(url)
    }

    override fun onResume() {
        logInfo("onResume")
        super.onResume()
        if (customTabLaunched) {
            finish()
        } else {
            customTabLaunched = true
        }
    }

    private fun startCustomTabForExternalPayment(url: String) {
        val intent = CustomTabsIntent.Builder().build()

        intent.launchUrl(this, Uri.parse(url))
    }

    override fun onDestroy() {
        removeExternalPaymentResponseStreamCollector()
        logInfo("onDestroy")
        setResult(RESULT_OK)
        super.onDestroy()
    }

    override fun accept() {
        logInfo("accept")
        startActivity(newIntent(this, null, true))
    }

    private fun observeExternalPaymentResponseStream() {
        ExternalPaymentResponseStream.getInstance().collect(this)
    }

    private fun removeExternalPaymentResponseStreamCollector() {
        ExternalPaymentResponseStream.getInstance().removeCollector(this)
    }

    companion object {
        private const val URL = "URL"
        private const val SHOULD_CLOSE_ACTIVITY = "SHOULD_CLOSE_ACTIVITY"

        @JvmStatic
        fun newIntent(
            context: Context,
            url: String?,
            shouldCloseActivity: Boolean = false
        ): Intent {
            val intent = Intent(context, ExternalPaymentActivity::class.java)
            intent.putExtra(URL, url)
            intent.putExtra(SHOULD_CLOSE_ACTIVITY, shouldCloseActivity)
            if (shouldCloseActivity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            return intent
        }
    }
}
