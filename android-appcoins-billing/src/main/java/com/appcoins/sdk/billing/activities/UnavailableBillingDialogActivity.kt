package com.appcoins.sdk.billing.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.core.logger.Logger.logInfo

class UnavailableBillingDialogActivity : Activity() {

    private val sdkAnalytics = SdkAnalyticsUtils.sdkAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.unavailable_billing_dialog_activity)

        logInfo("Starting UnavailableBillingDialogActivity.")

        setupMessage()
        setupButtons()
    }

    private fun setupMessage() {
        findViewById<TextView>(R.id.text_view_unavailable_billing_error_message)?.let {
            val errorMessage = getErrorMessage()
            it.text = errorMessage ?: resources.getString(R.string.sdk_unavailable_description_body)
        }
    }

    override fun finish() {
        PaymentResponseStream.getInstance()
            .emit(SDKPaymentResponse.createServiceUnavailableResponse())
        super.finish()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.button_close)?.let {
            it.setOnClickListener { finish() }
        }
    }

    private fun getErrorMessage(): String? =
        intent.getStringExtra(ERROR_MESSAGE)

    companion object {
        private const val ERROR_MESSAGE = "ERROR_MESSAGE"

        @JvmStatic
        fun newIntent(context: Context, errorMessage: String?): Intent {
            val intent = Intent(context, UnavailableBillingDialogActivity::class.java)
            intent.putExtra(ERROR_MESSAGE, errorMessage?.takeIf { it.isNotEmpty() })
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            return intent
        }
    }
}
