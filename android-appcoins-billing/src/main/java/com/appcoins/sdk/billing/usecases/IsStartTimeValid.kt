package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.core.logger.Logger.logError
import java.util.Date

object IsStartTimeValid : UseCase() {
    operator fun invoke(startTimeInMillis: Long?): Boolean {
        super.invokeUseCase()

        return try {
            startTimeInMillis?.let { Date(it).time > 0 } ?: true
        } catch (ex: Exception) {
            logError("There was an error validating startTime provided.", ex)
            false
        }
    }
}
