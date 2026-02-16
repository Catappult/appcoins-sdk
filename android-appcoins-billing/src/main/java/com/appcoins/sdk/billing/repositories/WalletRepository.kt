package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.mappers.WalletDetailsMapper
import com.appcoins.sdk.billing.models.WalletDetails
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WalletRepository(private val service: BdsService) {

    fun requestWalletSync(id: String): WalletDetails {
        val countDownLatch = CountDownLatch(1)
        var walletDetails = WalletDetails.createErrorWalletDetails()

        val queries: MutableMap<String, String> = HashMap()
        queries["id"] = id

        val serviceResponseListener = ServiceResponseListener { requestResponse: RequestResponse? ->
            val walletDetailsResponse = WalletDetailsMapper().map(requestResponse!!)
            walletDetails =
                WalletDetails(
                    walletDetailsResponse.walletAddress,
                    walletDetailsResponse.walletToken,
                    walletDetailsResponse.expirationTimeMillis,
                    walletDetailsResponse.hasError()
                )
            countDownLatch.countDown()
        }

        service.makeRequest(
            "/appc/1.20251009/guest_wallet",
            "GET",
            ArrayList(),
            queries,
            emptyMap(),
            emptyMap(),
            serviceResponseListener,
            SdkBackendRequestType.GUEST_WALLET
        )

        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout for Wallet Request: $e")
        }
        return walletDetails
    }
}
