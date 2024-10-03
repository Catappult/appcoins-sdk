package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.mappers.InappPurchaseResponse
import com.appcoins.sdk.billing.mappers.InappPurchaseResponseMapper
import com.appcoins.sdk.billing.mappers.PurchaseResponse
import com.appcoins.sdk.billing.mappers.PurchaseResponseMapper
import com.appcoins.sdk.billing.mappers.PurchasesResponse
import com.appcoins.sdk.billing.mappers.PurchasesResponseMapper
import com.appcoins.sdk.billing.mappers.SkuDetailsResponse
import com.appcoins.sdk.billing.mappers.SkuDetailsResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ProductV2Repository(private val bdsService: BdsService) {

    fun getInappPurchase(purchaseToken: String): InappPurchaseResponse? {
        val countDownLatch = CountDownLatch(1)
        var inappPurchase: InappPurchaseResponse? = null

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                requestResponse?.let {
                    val inappPurchaseResponse = InappPurchaseResponseMapper().map(requestResponse)
                    inappPurchaseResponse.responseCode?.let { responseCode ->
                        if (ServiceUtils.isSuccess(responseCode)) {
                            inappPurchase = inappPurchaseResponse
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
        return inappPurchase
    }

    fun getPurchasesSync(
        packageName: String,
        walletAddress: String,
        signedWallet: String,
        type: String
    ): PurchasesResponse {
        val countDownLatch = CountDownLatch(1)
        var purchasesResponse = PurchasesResponse(ResponseCode.ERROR.value)

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val purchasesModel = PurchasesResponseMapper().map(requestResponse)
                purchasesResponse = purchasesModel
                countDownLatch.countDown()
            }

        val queries: MutableMap<String, String> = HashMap()
        queries["wallet.address"] = walletAddress
        queries["wallet.signature"] = signedWallet
        queries["type"] = type
        queries["state"] = "PENDING"

        bdsService.makeRequest(
            "/productv2/8.20240901/applications/$packageName/inapp/consumable/purchases",
            "GET",
            emptyList(),
            queries,
            emptyMap(),
            emptyMap(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return purchasesResponse
    }

    fun getPurchaseSync(
        packageName: String,
        authorization: String,
        purchaseToken: String
    ): PurchaseResponse? {
        val countDownLatch = CountDownLatch(1)
        var purchaseResponse: PurchaseResponse? = null

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val purchaseModel = PurchaseResponseMapper().map(requestResponse)
                purchaseResponse = purchaseModel
                countDownLatch.countDown()
            }

        val headers: MutableMap<String, String> = HashMap()
        headers["authorization"] = "Bearer $authorization"

        bdsService.makeRequest(
            "/productv2/8.20240901/applications/$packageName/inapp/consumable/purchases/$purchaseToken",
            "GET",
            emptyList(),
            emptyMap(),
            headers,
            emptyMap(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return purchaseResponse
    }

    fun consumePurchaseSync(
        walletAddress: String,
        signature: String,
        packageName: String,
        purchaseToken: String
    ): Int {
        val countDownLatch = CountDownLatch(1)
        val responseCode = intArrayOf(ResponseCode.ERROR.value)

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                handleConsumeResponse(
                    requestResponse,
                    countDownLatch,
                    responseCode
                )
            }

        val queries: MutableMap<String, String> = HashMap()
        queries["wallet.address"] = walletAddress
        queries["wallet.signature"] = signature

        bdsService.makeRequest(
            "/productv2/8.20240901/applications/$packageName/inapp/purchases/$purchaseToken/consume",
            "POST",
            emptyList(),
            queries,
            emptyMap(),
            emptyMap(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return responseCode[0]
    }

    fun getSkuDetails(
        packageName: String,
        skus: List<String>,
        paymentFlow: String? = null
    ): SkuDetailsResponse? {
        val countDownLatch = CountDownLatch(1)
        var skuDetailsResponse: SkuDetailsResponse? = null

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                skuDetailsResponse = SkuDetailsResponseMapper().map(requestResponse)
                countDownLatch.countDown()
            }

        val queries: MutableMap<String, String> = HashMap()
        queries["skus"] = skus.joinToString(",")
        paymentFlow?.let { queries["discount_policy"] = it }

        bdsService.makeRequest(
            "/productv2/8.20240901/applications/$packageName/inapp/consumables",
            "GET",
            emptyList(),
            queries,
            emptyMap(),
            emptyMap(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return skuDetailsResponse
    }

    private fun handleConsumeResponse(
        requestResponse: RequestResponse, countDownLatch: CountDownLatch,
        responseCode: IntArray
    ) {
        if (ServiceUtils.isSuccess(requestResponse.responseCode)) {
            responseCode[0] = ResponseCode.OK.value
        } else {
            responseCode[0] = ResponseCode.ERROR.value
        }
        countDownLatch.countDown()
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout getting Purchase from ProductV2: $e")
        }
    }
}
