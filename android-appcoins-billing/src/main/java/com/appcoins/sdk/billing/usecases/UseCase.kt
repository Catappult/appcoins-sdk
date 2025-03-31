package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.core.logger.Logger.logInfo

abstract class UseCase {
    fun invokeUseCase() {
        logInfo("UseCase ${this::class.simpleName} invoked.")
    }
}
