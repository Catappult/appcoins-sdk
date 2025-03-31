package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.FeatureType
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod

object GetDefaultFeaturesSupported : UseCase() {

    operator fun invoke(paymentFlowMethod: PaymentFlowMethod?): List<FeatureType>? {
        super.invokeUseCase()
        return paymentFlowMethod?.let {
            when (paymentFlowMethod) {
                is PaymentFlowMethod.WebPayment -> listOf()
                is PaymentFlowMethod.Wallet -> listOf(FeatureType.SUBSCRIPTIONS)
                is PaymentFlowMethod.AptoideGames -> listOf()
                is PaymentFlowMethod.GamesHub -> listOf()
                is PaymentFlowMethod.UnavailableBilling -> listOf()
            }
        }
    }
}
