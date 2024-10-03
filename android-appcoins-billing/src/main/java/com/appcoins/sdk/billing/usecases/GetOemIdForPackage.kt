package com.appcoins.sdk.billing.usecases

import android.content.Context
import com.appcoins.sdk.billing.oemid.OemIdExtractorV1
import com.appcoins.sdk.billing.oemid.OemIdExtractorV2
import com.appcoins.sdk.billing.service.OemIdExtractorService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences

object GetOemIdForPackage : UseCase() {
    operator fun invoke(packageName: String?, context: Context): String? {
        super.invokeUseCase()
        val attributionSharedPreferences = AttributionSharedPreferences(context)

        return attributionSharedPreferences.getOemId()
            ?: OemIdExtractorService(
                OemIdExtractorV1(context),
                OemIdExtractorV2(context)
            ).extractOemId(packageName)
    }
}
