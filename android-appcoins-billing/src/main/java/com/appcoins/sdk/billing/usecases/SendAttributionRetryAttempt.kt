package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.helpers.WalletUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SendAttributionRetryAttempt : UseCase() {

    operator fun invoke(attempts: Int, timestamp: Long) {
        super.invokeUseCase()

        val initialDate = Date(timestamp)
        val currentDate = Date(System.currentTimeMillis())

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val message =
            "Attempting to request Attribution for $attempts attempt. " +
                "Initial date of Attribution: ${formatter.format(initialDate)}. " +
                "Current date: ${formatter.format(currentDate)}."

        WalletUtils.getSdkAnalytics().sendAttributionRetryAttemptEvent(message)
    }
}