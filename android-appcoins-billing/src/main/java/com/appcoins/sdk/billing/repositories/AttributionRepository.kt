package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.billing.mappers.AttributionResponse
import com.appcoins.sdk.billing.mappers.AttributionResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AttributionRepository(private val bdsService: BdsService) {

    fun getAttributionForUser(
        packageName: String,
        oemId: String?,
        guestWalletId: String?,
        initialAttributionTimestamp: Long,
    ): AttributionResponse? {
        val countDownLatch = CountDownLatch(1)
        var attributionResponse: AttributionResponse? = null

        val queries: MutableMap<String, String> = LinkedHashMap()
        queries["package_name"] = packageName
        oemId?.let { queries["oemid"] = it }
        guestWalletId?.let { queries["guest_uid"] = it }
        queries["timestamp"] = initialAttributionTimestamp.toString()

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
            serviceResponseListener,
            SdkBackendRequestType.ATTRIBUTION
        )

        waitForCountDown(countDownLatch)
        return attributionResponse
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout getting User Attribution: $e")
        }
    }
}
