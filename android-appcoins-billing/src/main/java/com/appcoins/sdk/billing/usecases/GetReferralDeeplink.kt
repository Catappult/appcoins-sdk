package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.ReferralDeeplink
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.managers.StoreLinkMapperManager
import com.appcoins.sdk.core.logger.Logger.logInfo

object GetReferralDeeplink : UseCase() {

    private val storeLinkMapperManager by lazy {
        StoreLinkMapperManager(WalletUtils.context)
    }

    operator fun invoke(): ReferralDeeplink {
        super.invokeUseCase()

        val referralDeeplinkResponse = storeLinkMapperManager.getReferralDeeplink()

        logInfo(
            "Received ReferralDeeplink. ResponseCode: ${referralDeeplinkResponse.responseCode} " +
                "| StoreDeeplink: ${referralDeeplinkResponse.storeDeeplink} " +
                "| FallbackDeeplink: ${referralDeeplinkResponse.fallbackDeeplink}"
        )

        return ReferralDeeplink(
            ResponseCode.fromValue(referralDeeplinkResponse.responseCode) ?: ResponseCode.ERROR,
            referralDeeplinkResponse.storeDeeplink,
            referralDeeplinkResponse.fallbackDeeplink
        )
    }
}
