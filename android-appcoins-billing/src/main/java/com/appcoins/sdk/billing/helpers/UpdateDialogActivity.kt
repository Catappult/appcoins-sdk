package com.appcoins.sdk.billing.helpers

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.analytics.SdkUpdateFlowActions
import com.appcoins.sdk.billing.usecases.ingameupdates.LaunchAppUpdate
import com.appcoins.sdk.core.logger.Logger.logInfo

class UpdateDialogActivity : Activity() {

    private val sdkAnalytics = WalletUtils.getSdkAnalytics()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.update_dialog_activity)

        logInfo("Starting UpdateDialogActivity.")

        sdkAnalytics.appUpdateImpression()
        setActionsForButtons()
    }

    override fun onBackPressed() {
        logInfo("User BACK_PRESSED on UpdateDialogActivity.")
        sdkAnalytics.appUpdateClick(SdkUpdateFlowActions.BACK_PRESSED)
        super.onBackPressed()
    }

    private fun setActionsForButtons() {
        findViewById<Button>(R.id.button_update)?.let {
            it.setOnClickListener {
                logInfo("User pressed UPDATE_APP on UpdateDialogActivity.")
                sdkAnalytics.appUpdateClick(SdkUpdateFlowActions.UPDATE_APP)
                Thread { LaunchAppUpdate(applicationContext) }.start()
                finish()
            }
        }
        findViewById<Button>(R.id.button_close)?.let {
            it.setOnClickListener {
                logInfo("User pressed CANCEL on UpdateDialogActivity.")
                sdkAnalytics.appUpdateClick(SdkUpdateFlowActions.CANCEL)
                finish()
            }
        }
    }
}
