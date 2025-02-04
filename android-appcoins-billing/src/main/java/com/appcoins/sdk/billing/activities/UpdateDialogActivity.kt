package com.appcoins.sdk.billing.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.usecases.ingameupdates.LaunchAppUpdate
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkLaunchAppUpdateDialogLabels
import com.appcoins.sdk.core.logger.Logger.logInfo

class UpdateDialogActivity : Activity() {

    private val sdkAnalytics = SdkAnalyticsUtils.sdkAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.update_dialog_activity)

        logInfo("Starting UpdateDialogActivity.")

        sdkAnalytics.sendLaunchAppUpdateDialogRequestEvent()
        setActionsForButtons()
    }

    override fun onBackPressed() {
        logInfo("User BACK_PRESSED on UpdateDialogActivity.")
        sdkAnalytics.sendLaunchAppUpdateDialogActionEvent(SdkLaunchAppUpdateDialogLabels.BACK_BUTTON)
        super.onBackPressed()
    }

    private fun setActionsForButtons() {
        findViewById<Button>(R.id.button_update)?.let {
            it.setOnClickListener {
                logInfo("User pressed UPDATE_APP on UpdateDialogActivity.")
                sdkAnalytics.sendLaunchAppUpdateDialogActionEvent(SdkLaunchAppUpdateDialogLabels.UPDATE)
                Thread { LaunchAppUpdate(applicationContext) }.start()
                finish()
            }
        }
        findViewById<Button>(R.id.button_close)?.let {
            it.setOnClickListener {
                logInfo("User pressed CANCEL on UpdateDialogActivity.")
                sdkAnalytics.sendLaunchAppUpdateDialogActionEvent(SdkLaunchAppUpdateDialogLabels.CLOSE)
                finish()
            }
        }
    }
}
