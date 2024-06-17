package com.appcoins.sdk.ingameupdates.repositories

import com.appcoins.sdk.ingameupdates.mappers.StoreLinkResponseMapper
import com.appcoins.sdk.ingameupdates.services.BdsService
import com.appcoins.sdk.ingameupdates.services.RequestResponse
import com.appcoins.sdk.ingameupdates.services.ServiceResponseListener
import com.appcoins.sdk.ingameupdates.utils.ServiceUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class StoreDeepLinkRepository(private val bdsService: BdsService) {

    fun getStoreDeepLink(
        packageName: String,
        appInstallerPackageName: String,
    ): String? {
        val countDownLatch = CountDownLatch(1)
        var storeDeepLink: String? = null

        val queries: MutableMap<String, String> = LinkedHashMap()
        queries["store-package"] = appInstallerPackageName

        val serviceResponseListener =
            object : ServiceResponseListener {
                override fun onResponseReceived(requestResponse: RequestResponse?) {
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
            e.printStackTrace()
        }
    }
}
