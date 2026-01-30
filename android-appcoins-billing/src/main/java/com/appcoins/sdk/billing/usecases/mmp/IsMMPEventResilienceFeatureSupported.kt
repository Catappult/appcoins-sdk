package com.appcoins.sdk.billing.usecases.mmp

import com.appcoins.sdk.billing.managers.FeatureFlagsManager
import com.appcoins.sdk.billing.payflow.models.featureflags.FeatureFlag
import com.appcoins.sdk.billing.usecases.UseCase

object IsMMPEventResilienceFeatureSupported : UseCase() {

    operator fun invoke(): Boolean {
        super.invokeUseCase()
        return FeatureFlagsManager.getFeatureFlag(FeatureFlag.Feature.MMP_PURCHASE_RESILIENCE)?.active ?: false
    }
}
