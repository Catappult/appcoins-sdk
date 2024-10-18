package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.mappers.VoidedPurchaseResponse
import com.appcoins.sdk.billing.mappers.VoidedPurchaseResponseMapper
import com.appcoins.sdk.billing.mappers.VoidedPurchasesResponse
import com.appcoins.sdk.billing.mappers.VoidedPurchasesResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class VoidedPurchasesRepository(private val bdsService: BdsService) {

    fun getVoidedPurchasesSync(
        packageName: String,
        authorization: String,
        startTime: String?
    ): VoidedPurchasesResponse {
        val countDownLatch = CountDownLatch(1)
        var voidedPurchasesResponse = VoidedPurchasesResponse(ResponseCode.ERROR.value)

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val voidedPurchasesResponse1 = VoidedPurchasesResponseMapper().map(requestResponse)
                voidedPurchasesResponse = voidedPurchasesResponse1
                countDownLatch.countDown()
            }

        val queries: MutableMap<String, String> = HashMap()
        startTime?.let { queries["startTime"] = it }

        val headers: MutableMap<String, String> = HashMap()
        headers["authorization"] = "Bearer $authorization"

        bdsService.makeRequest(
            "/productv2/8.20240901/google/inapp/v3/applications/$packageName/purchases/voidedpurchases",
            "GET",
            emptyList(),
            queries,
            headers,
            emptyMap(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return voidedPurchasesResponse
    }

    fun getVoidedPurchaseSync(
        packageName: String,
        authorization: String,
        purchaseToken: String
    ): VoidedPurchaseResponse? {
        val countDownLatch = CountDownLatch(1)
        var voidedPurchaseResponse: VoidedPurchaseResponse? = null

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val voidedPurchaseModel = VoidedPurchaseResponseMapper().map(requestResponse)
                voidedPurchaseResponse = voidedPurchaseModel
                countDownLatch.countDown()
            }

        val headers: MutableMap<String, String> = HashMap()
        headers["authorization"] = "Bearer $authorization"

        bdsService.makeRequest(
            "/productv2/8.20240901/google/inapp/v3/applications/$packageName/purchases/voidedpurchases/$purchaseToken",
            "GET",
            emptyList(),
            emptyMap(),
            headers,
            emptyMap(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return voidedPurchaseResponse
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout getting Voided Purchases: $e")
        }
    }
}
