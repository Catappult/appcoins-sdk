package com.appcoins.sdk.billing.usecases

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.sharedpreferences.SystemSharedPreferences
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.logger.Logger.logInfo

object VerifyDontKeepActivitiesStatus : UseCase() {

    private val systemSharedPreferences: SystemSharedPreferences by lazy {
        SystemSharedPreferences(WalletUtils.context)
    }

    operator fun invoke() {
        super.invokeUseCase()

        val isDontKeepActivitiesEventSent = systemSharedPreferences.isDontKeepActivitiesEventSent()

        if (!isDontKeepActivitiesEventSent) {
            val isAlwaysFinishActivitiesEnabled = isAlwaysFinishActivitiesEnabled(WalletUtils.context)
            if (isAlwaysFinishActivitiesEnabled) {
                logInfo("Always finish activities is enabled. Sending dont keep activities event.")
                systemSharedPreferences.sendDontKeepActivitiesEvent()
                sendDontKeepActivitiesEvent()
            }
        } else {
            logInfo("Dont keep activities event is already sent.")
        }
    }

    private fun sendDontKeepActivitiesEvent() {
        SdkAnalyticsUtils.sdkAnalytics.sendDoNotKeepActivitiesEvent()
    }

    private fun isAlwaysFinishActivitiesEnabled(context: Context): Boolean {
        val resolver: ContentResolver = context.contentResolver
        val state = Settings.Global.getInt(
            resolver,
            Settings.Global.ALWAYS_FINISH_ACTIVITIES,
            0
        )
        return state != 0
    }
}
