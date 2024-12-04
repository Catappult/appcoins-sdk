package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.mappers.WalletGenerationMapper
import com.appcoins.sdk.billing.models.WalletGenerationModel
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.service.Service
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WalletRepository(private val service: Service) {

    fun requestWalletSync(id: String): WalletGenerationModel {
        val countDownLatch = CountDownLatch(1)
        var walletGenerationModel = WalletGenerationModel.createErrorWalletGenerationModel()

        val queries: MutableMap<String, String> = HashMap()
        queries["id"] = id

        val serviceResponseListener = ServiceResponseListener { requestResponse: RequestResponse? ->
            val walletGenerationResponse = WalletGenerationMapper().map(requestResponse!!)
            walletGenerationModel =
                WalletGenerationModel(
                    walletGenerationResponse.address,
                    walletGenerationResponse.signature,
                    walletGenerationResponse.ewt,
                    walletGenerationResponse.hasError()
                )
            countDownLatch.countDown()
        }

        service.makeRequest(
            "/appc/guest_wallet",
            "GET",
            ArrayList(),
            queries,
            emptyMap(),
            emptyMap(),
            serviceResponseListener
        )

        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout for Wallet Request: $e")
        }
        return walletGenerationModel
    }
}
