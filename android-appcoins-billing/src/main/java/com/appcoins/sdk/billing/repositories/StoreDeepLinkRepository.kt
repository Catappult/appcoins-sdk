package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.mappers.StoreLinkResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class StoreDeepLinkRepository(private val bdsService: BdsService) {

    fun getStoreDeepLink(
        packageName: String,
        appInstallerPackageName: String?,
    ): String? {
        val countDownLatch = CountDownLatch(1)
        var storeDeepLink: String? = null

        val queries: MutableMap<String, String> = LinkedHashMap()
        appInstallerPackageName?.let { queries["store-package"] = it }

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                requestResponse?.let {
                    val webPaymentUrlResponse = StoreLinkResponseMapper().map(requestResponse)
                    webPaymentUrlResponse.responseCode?.let { responseCode ->
                        if (ServiceUtils.isSuccess(responseCode)) {
                            storeDeepLink = webPaymentUrlResponse.deeplink
                        }
                    }
                }
                countDownLatch.countDown()
            }

        bdsService.makeRequest(
            "/deeplink/$packageName",
            "GET",
            emptyList(),
            queries.toMap(),
            emptyMap(),
            emptyMap<String?, Any>(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return storeDeepLink
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logError("Timeout getting Store Deeplink: $e")
        }
    }
}
