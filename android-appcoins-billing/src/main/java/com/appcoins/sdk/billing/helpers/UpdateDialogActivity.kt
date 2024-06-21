package com.appcoins.sdk.billing.helpers

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.analytics.SdkUpdateFlowActions
import com.appcoins.sdk.billing.usecases.ingameupdates.LaunchAppUpdate

class UpdateDialogActivity : Activity() {

    private val sdkAnalytics = WalletUtils.getSdkAnalytics()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.update_dialog_activity)

        //This log is necessary for the automatic test that validates the wallet installation dialog
        Log.d(
            "UpdateDialogActivity",
            "com.appcoins.sdk.billing.helpers.UpdateDialogActivity started"
        )

        sdkAnalytics.appUpdateImpression()
        setActionsForButtons()
    }

    override fun onBackPressed() {
        sdkAnalytics.appUpdateClick(SdkUpdateFlowActions.BACK_PRESSED)
        super.onBackPressed()
    }

    private fun setActionsForButtons() {
        findViewById<Button>(R.id.button_update)?.let {
            it.setOnClickListener {
                sdkAnalytics.appUpdateClick(SdkUpdateFlowActions.UPDATE_APP)
                Thread { LaunchAppUpdate.invoke(applicationContext) }.start()
                finish()
            }
        }
        findViewById<Button>(R.id.button_close)?.let {
            it.setOnClickListener {
                sdkAnalytics.appUpdateClick(SdkUpdateFlowActions.CANCEL)
                finish()
            }
        }
    }
}
