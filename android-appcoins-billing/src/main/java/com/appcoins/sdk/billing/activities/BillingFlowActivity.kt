package com.appcoins.sdk.billing.activities

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.listeners.PaymentResponseStream
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse
import com.appcoins.sdk.core.logger.Logger.logInfo


class BillingFlowActivity : AppCompatActivity() {

    private val content =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            logInfo("Extras: " + activityResult.data?.extras)
            logInfo("ResultCode: " + activityResult.resultCode)
            logInfo("Extras: ")
            PaymentResponseStream.getInstance().emit(
                SDKPaymentResponse(activityResult.resultCode, activityResult.data)
            )
            finish()
        }

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
            finish()
            return
        }

        val intentSender = pendingIntent.intentSender

        val intentSenderRequest =
            IntentSenderRequest.Builder(intentSender)
                .setFillInIntent(Intent())
                .setFlags(0, 0)
                .build()

        content.launch(intentSenderRequest)
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