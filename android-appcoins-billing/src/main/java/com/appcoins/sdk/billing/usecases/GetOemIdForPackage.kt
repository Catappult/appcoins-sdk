package com.appcoins.sdk.billing.usecases

import android.content.Context
import com.appcoins.sdk.billing.payasguest.oemid.OemIdExtractorV1
import com.appcoins.sdk.billing.payasguest.oemid.OemIdExtractorV2
import com.appcoins.sdk.billing.service.address.OemIdExtractorService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences

class GetOemIdForPackage {
    companion object {
        fun invoke(packageName: String?, context: Context): String? {
            val attributionSharedPreferences = AttributionSharedPreferences(context)

            return attributionSharedPreferences.getOemId()
                ?: OemIdExtractorService(OemIdExtractorV1(context), OemIdExtractorV2(context))
                    .extractOemId(packageName)
        }
    }
}
