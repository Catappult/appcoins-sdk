package com.appcoins.sdk.billing.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.KEY_BUY_INTENT_RAW
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESULT_CODE
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

class BillingFlowActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transparent_activity)

        try {
            val bundle = getBundleFromExtras()

            if (bundle == null) {
                logInfo("Bundle from extras not found. Sending FAILURE response for payment.")
                PaymentResponseStream.getInstance()
                    .emit(SDKPaymentResponse.createErrorTypeResponse())
                finish()
                return
            }

            val intent = getBuyIntentFromBundle(bundle)

            if (intent == null) {
                logInfo("Buy Intent from bundle not found. Sending FAILURE response for payment.")
                PaymentResponseStream.getInstance()
                    .emit(SDKPaymentResponse.createErrorTypeResponse())
                finish()
                return
            }
            logInfo("Starting Billing Flow intent package: ${intent.`package`}")
            startActivityForResult(intent, RESULT_CODE)
        } catch (ex: Exception) {
            logError("Failed to start payment activity.", ex)
            PaymentResponseStream.getInstance().emit(SDKPaymentResponse.createErrorTypeResponse())
            finish()
        }
    }

    @Suppress("deprecation")
    private fun getBundleFromExtras(): Bundle? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BUY_BUNDLE, Bundle::class.java)
        } else {
            intent.getParcelableExtra(BUY_BUNDLE)
        }

    @Suppress("deprecation")
    private fun getBuyIntentFromBundle(bundle: Bundle): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(KEY_BUY_INTENT_RAW, Intent::class.java)
        } else {
            bundle.getParcelable(KEY_BUY_INTENT_RAW)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logInfo(
            "Received response from Billing Flow.\nRequest Code: $requestCode\nResult Code: $resultCode"
        )
        logDebug("Extras: " + data?.extras)
        PaymentResponseStream.getInstance().emit(SDKPaymentResponse(resultCode, data))
        finish()
    }

    companion object {
        private const val BUY_BUNDLE = "BUY_BUNDLE"

        @JvmStatic
        fun newIntent(context: Context, bundle: Bundle): Intent {
            val intent = Intent(context, BillingFlowActivity::class.java)
            intent.putExtra(BUY_BUNDLE, bundle)
            return intent
        }
    }
}
