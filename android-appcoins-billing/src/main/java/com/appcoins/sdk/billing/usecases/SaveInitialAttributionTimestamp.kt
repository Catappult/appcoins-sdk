package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences

object SaveInitialAttributionTimestamp : UseCase() {
    private val attributionSharedPreferences by lazy {
        AttributionSharedPreferences(WalletUtils.context)
    }

    operator fun invoke() {
        super.invokeUseCase()
        if (attributionSharedPreferences.getInitialAttributionTimestamp() == 0L) {
            attributionSharedPreferences.setInitialAttributionTimestamp(System.currentTimeMillis())
        }
    }
}
