package com.appcoins.sdk.billing.usecases

import android.content.Context
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.core.device.getDeviceInfo
import com.appcoins.sdk.core.logger.Logger.logInfo

object LogGeneralInformation : UseCase() {
    operator fun invoke(context: Context) {
        val deviceInformation = getDeviceInfo()

        logInfo(
            "GeneralSDKInformation [ " +
                "Package Name: ${context.packageName} " +
                "| OSVersion: ${deviceInformation.osVersion} " +
                "| Brand: ${deviceInformation.brand} " +
                "| Model: ${deviceInformation.model} " +
                "| Language: ${deviceInformation.language} " +
                "| Is Probably Emulator: ${deviceInformation.isProbablyEmulator} " +
                "| SDK Version: ${BuildConfig.VERSION_CODE} " +
                "]"
        )
    }
}
