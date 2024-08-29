package com.appcoins.sdk.billing.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.KEY_BUY_INTENT
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
                PaymentResponseStream.getInstance().emit(SDKPaymentResponse(1))
                finish()
                return
            }

            val pendingIntent = getPendingIntentFromBundle(bundle)

            if (pendingIntent == null) {
                logInfo("PendingIntent from bundle not found. Sending FAILURE response for payment.")
                PaymentResponseStream.getInstance().emit(SDKPaymentResponse(1))
                finish()
                return
            }
            logInfo("Starting Billing Flow intent sender: ${pendingIntent.intentSender.creatorPackage}")
            startIntentSenderForResult(
                pendingIntent.intentSender,
                RESULT_CODE,
                null,
                0,
                0,
                0
            )
        } catch (ex: Exception) {
            logError("Failed to start payment activity.", ex)
            PaymentResponseStream.getInstance().emit(SDKPaymentResponse(1))
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
    private fun getPendingIntentFromBundle(bundle: Bundle): PendingIntent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(KEY_BUY_INTENT, PendingIntent::class.java)
        } else {
            bundle.getParcelable(KEY_BUY_INTENT)
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
