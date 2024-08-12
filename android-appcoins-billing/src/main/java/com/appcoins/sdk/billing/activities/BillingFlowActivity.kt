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
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESULT_CODE
import com.appcoins.sdk.core.logger.Logger.logInfo


class BillingFlowActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transparent_activity)

        val pendingIntent: PendingIntent? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BUY_INTENT, PendingIntent::class.java)
            } else {
                intent.getParcelableExtra(BUY_INTENT)
            }

        if (pendingIntent == null) {
            PaymentResponseStream.getInstance().emit(SDKPaymentResponse(1))
            finish()
            return
        }

        val intentSender = pendingIntent.intentSender

        startIntentSenderForResult(intentSender, RESULT_CODE, null, 0, 0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logInfo("Extras: " + data?.extras)
        logInfo("ResultCode: $resultCode")
        logInfo("Extras: ")
        PaymentResponseStream.getInstance().emit(SDKPaymentResponse(resultCode, data))
        finish()
    }

    companion object {
        private const val BUY_INTENT = "BUY_INTENT"

        @JvmStatic
        fun newIntent(context: Context, pendingIntent: PendingIntent): Intent {
            val intent = Intent(context, BillingFlowActivity::class.java)
            intent.putExtra(BUY_INTENT, pendingIntent)
            return intent
        }
    }

}