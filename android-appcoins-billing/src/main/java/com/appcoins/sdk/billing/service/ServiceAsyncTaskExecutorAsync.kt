package com.appcoins.sdk.billing.service

import android.os.Handler
import android.os.Looper
import com.appcoins.sdk.billing.analytics.SdkBackendRequestType
import java.util.concurrent.Executors

class ServiceAsyncTaskExecutorAsync(
    private val bdsService: BdsService,
    private val baseUrl: String,
    private val endPoint: String,
    private val httpMethod: String,
    private val paths: MutableList<String>,
    private val queries: MutableMap<String, String>,
    private val header: MutableMap<String, String>,
    private val body: MutableMap<String, out Any>,
    private val serviceResponseListener: ServiceResponseListener?,
    private val sdkBackendRequestType: SdkBackendRequestType
) {
    fun execute() {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            val requestResponse =
                bdsService.createRequest(
                    baseUrl,
                    endPoint,
                    httpMethod,
                    paths,
                    queries,
                    header,
                    body,
                    sdkBackendRequestType
                )
            handler.post {
                serviceResponseListener?.onResponseReceived(requestResponse)
            }
        }
    }
}
