package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.mappers.AppVersionResponseMapper
import com.appcoins.sdk.billing.mappers.Version
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AppVersionRepository(private val bdsService: BdsService) {

    fun getAppVersions(
        packageName: String,
    ): List<Version>? {
        val countDownLatch = CountDownLatch(1)
        var appVersions: List<Version>? = null

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
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
            logError("Failed to get App Version: $e")
        }
    }
}
