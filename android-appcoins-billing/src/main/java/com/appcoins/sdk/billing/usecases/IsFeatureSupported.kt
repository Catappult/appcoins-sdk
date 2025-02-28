package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.FeatureType
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.helpers.WalletUtils.currentPaymentFlowMethod
import com.appcoins.sdk.core.logger.Logger.logInfo

object IsFeatureSupported : UseCase() {

    operator fun invoke(feature: FeatureType): ResponseCode {
        super.invokeUseCase()
        return currentPaymentFlowMethod?.availableFeatures?.let {
            if (it.contains(feature)) {
                logInfo("Feature is supported.")
                ResponseCode.OK
            } else {
                logInfo("Feature not found in the Supported Features.")
                ResponseCode.FEATURE_NOT_SUPPORTED
            }
        } ?: isFeatureSupportedInDefaultValues(feature)
    }

    private fun isFeatureSupportedInDefaultValues(feature: FeatureType): ResponseCode {
        return GetDefaultFeaturesSupported.invoke(currentPaymentFlowMethod)?.let {
            if (it.contains(feature)) {
                logInfo("Feature is supported.")
                ResponseCode.OK
            } else {
                logInfo("Feature not found in the Supported Features.")
                ResponseCode.FEATURE_NOT_SUPPORTED
            }
        } ?: ResponseCode.FEATURE_NOT_SUPPORTED
    }
}
