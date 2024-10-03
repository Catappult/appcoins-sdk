package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.mappers.AttributionResponse
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

object SaveAttributionResultOnPrefs : UseCase() {

    private val attributionSharedPreferences by lazy {
        AttributionSharedPreferences(WalletUtils.context)
    }

    operator fun invoke(attributionResponse: AttributionResponse) {
        super.invokeUseCase()
        attributionResponse.apply {
            processOemIdFromAttribution()
            processUtmSourceFromAttribution()
            processUtmMediumFromAttribution()
            processUtmCampaignFromAttribution()
            processUtmTermFromAttribution()
            processUtmContentFromAttribution()
            processWalletIdFromAttribution()
        }
    }

    private fun AttributionResponse.processOemIdFromAttribution() {
        oemId?.let {
            if (it.isNotEmpty()) {
                logInfo("Setting new OEMID.")
                logDebug("OEMID: $it")
                attributionSharedPreferences.setOemId(it)
            }
        }
    }

    private fun AttributionResponse.processUtmSourceFromAttribution() {
        utmSource?.let {
            if (it.isNotEmpty()) {
                logInfo("Setting new UtmSource.")
                logDebug("UtmSource: $it")
                attributionSharedPreferences.setUtmSource(it)
            }
        }
    }

    private fun AttributionResponse.processUtmMediumFromAttribution() {
        utmMedium?.let {
            if (it.isNotEmpty()) {
                logInfo("Setting new UtmMedium.")
                logDebug("UtmMedium: $it")
                attributionSharedPreferences.setUtmMedium(it)
            }
        }
    }

    private fun AttributionResponse.processUtmCampaignFromAttribution() {
        utmCampaign?.let {
            if (it.isNotEmpty()) {
                logInfo("Setting new UtmCampaign.")
                logDebug("UtmCampaign: $it")
                attributionSharedPreferences.setUtmCampaign(it)
            }
        }
    }

    private fun AttributionResponse.processUtmTermFromAttribution() {
        utmTerm?.let {
            if (it.isNotEmpty()) {
                logInfo("Setting new UtmTerm.")
                logDebug("UtmTerm: $it")
                attributionSharedPreferences.setUtmTerm(it)
            }
        }
    }

    private fun AttributionResponse.processUtmContentFromAttribution() {
        utmContent?.let {
            if (it.isNotEmpty()) {
                logInfo("Setting new UtmContent.")
                logDebug("UtmContent: $it")
                attributionSharedPreferences.setUtmContent(it)
            }
        }
    }

    private fun AttributionResponse.processWalletIdFromAttribution() {
        walletId?.let {
            if (it.isNotEmpty()) {
                logInfo("Setting new WalletId.")
                logDebug("WalletId: $it")
                attributionSharedPreferences.setWalletId(it)
            } else {
                sendBackendGuestUidGenerationFailedEvent()
            }
        } ?: sendBackendGuestUidGenerationFailedEvent()
    }

    private fun sendBackendGuestUidGenerationFailedEvent() {
        logError("Failure to get GuestUid for User from Attribution.")
        WalletUtils.getSdkAnalytics().sendBackendGuestUidGenerationFailedEvent()
    }
}
