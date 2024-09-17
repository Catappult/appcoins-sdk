package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.mappers.PurchaseResponse
import com.appcoins.sdk.billing.mappers.PurchaseResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ProductV2Repository(private val bdsService: BdsService) {

    fun getPurchase(purchaseToken: String): PurchaseResponse? {
        val countDownLatch = CountDownLatch(1)
        var purchase: PurchaseResponse? = null

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                requestResponse?.let {
                    val purchaseResponse = PurchaseResponseMapper().map(requestResponse)
                    purchaseResponse.responseCode?.let { responseCode ->
                        if (ServiceUtils.isSuccess(responseCode)) {
                            purchase = purchaseResponse
                        }
                    }
                }
                countDownLatch.countDown()
            }

        bdsService.makeRequest(
            "/productv2/8.20240901/inapp/purchases/$purchaseToken",
            "GET",
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap<String?, Any>(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return purchase
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout getting Purchase from ProductV2: $e")
        }
    }
}
