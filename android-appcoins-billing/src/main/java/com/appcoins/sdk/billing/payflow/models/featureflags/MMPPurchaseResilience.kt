package com.appcoins.sdk.billing.payflow.models.featureflags

class MMPPurchaseResilience(
    active: Boolean,
    severityLevel: SeverityLevel? = null
) : FeatureFlag(Feature.MMP_PURCHASE_RESILIENCE, active, severityLevel)
