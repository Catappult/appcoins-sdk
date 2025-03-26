package com.appcoins.sdk.billing.usecases.ingameupdates

import android.content.Context
import com.appcoins.sdk.billing.managers.StoreLinkMapperManager
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.logger.Logger.logError

object IsUpdateAvailable : UseCase() {

    operator fun invoke(context: Context): Boolean {
        super.invokeUseCase()

        return try {
            StoreLinkMapperManager(context).getNewVersionAvailability().isNewVersionAvailable
        } catch (e: Exception) {
            logError("Failed to verify if update available: $e")
            SdkAnalyticsUtils.sdkAnalytics.sendAppUpdateAvailableFailureToObtainResult()
            false
        }
    }
}
