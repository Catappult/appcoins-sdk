package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.FeatureType
import com.appcoins.sdk.billing.helpers.WalletUtils.currentPaymentFlowMethod
import com.appcoins.sdk.core.logger.Logger.logInfo

object IsFeatureSupported : UseCase() {

    operator fun invoke(feature: FeatureType): Boolean {
        super.invokeUseCase()
        return currentPaymentFlowMethod?.availableFeatures?.let {
            if (it.contains(feature)) {
                logInfo("Feature is supported.")
                true
            } else {
                logInfo("Feature not found in the Supported Features.")
                false
            }
        } ?: isFeatureSupportedInDefaultValues(feature)
    }

    private fun isFeatureSupportedInDefaultValues(feature: FeatureType): Boolean {
        return GetDefaultFeaturesSupported.invoke(currentPaymentFlowMethod)?.let {
            if (it.contains(feature)) {
                logInfo("Feature is supported.")
                true
            } else {
                logInfo("Feature not found in the local Supported Features.")
                false
            }
        } ?: false
    }
}
