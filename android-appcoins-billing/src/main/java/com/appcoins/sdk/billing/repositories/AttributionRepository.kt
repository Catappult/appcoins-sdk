package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.mappers.AttributionResponse
import com.appcoins.sdk.billing.mappers.AttributionResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AttributionRepository(private val bdsService: BdsService) {

    fun getAttributionForUser(
        packageName: String,
        oemId: String?,
        guestWalletId: String?,
    ): AttributionResponse? {
        val countDownLatch = CountDownLatch(1)
        var attributionResponse: AttributionResponse? = null

        val queries: MutableMap<String, String> = LinkedHashMap()
        queries["package_name"] = packageName
        oemId?.let { queries["oemid"] = it }
        guestWalletId?.let { queries["guest_uid"] = it }

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val attributionResponseMapped = AttributionResponseMapper().map(requestResponse)
                attributionResponseMapped.responseCode?.let { responseCode ->
                    if (ServiceUtils.isSuccess(responseCode)) {
                        attributionResponse = attributionResponseMapped
                    }
                }
                countDownLatch.countDown()
            }
        bdsService.makeRequest(
            "/attribution",
            "GET",
            emptyList(),
            queries,
            emptyMap(),
            emptyMap(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return attributionResponse
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}