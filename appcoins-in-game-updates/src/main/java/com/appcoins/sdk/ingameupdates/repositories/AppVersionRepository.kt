package com.appcoins.sdk.ingameupdates.repositories

import com.appcoins.sdk.ingameupdates.mappers.AppVersionResponseMapper
import com.appcoins.sdk.ingameupdates.mappers.Version
import com.appcoins.sdk.ingameupdates.services.BdsService
import com.appcoins.sdk.ingameupdates.services.RequestResponse
import com.appcoins.sdk.ingameupdates.services.ServiceResponseListener
import com.appcoins.sdk.ingameupdates.utils.ServiceUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AppVersionRepository(private val bdsService: BdsService) {

    fun getAppVersions(
        packageName: String,
    ): List<Version>? {
        val countDownLatch = CountDownLatch(1)
        var appVersions: List<Version>? = null

        val serviceResponseListener =
            object : ServiceResponseListener {
                override fun onResponseReceived(requestResponse: RequestResponse?) {
                    requestResponse?.let {
                        val appVersionResponse = AppVersionResponseMapper().map(requestResponse)
                        appVersionResponse.responseCode?.let { responseCode ->
                            if (ServiceUtils.isSuccess(responseCode)) {
                                appVersions = appVersionResponse.versions
                            }
                        }
                    }
                    countDownLatch.countDown()
                }
            }

        bdsService.makeRequest(
            "/app/get/store_name=catappult/package_name=$packageName/aab=1",
            "GET",
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap<String?, Any>(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return appVersions
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}