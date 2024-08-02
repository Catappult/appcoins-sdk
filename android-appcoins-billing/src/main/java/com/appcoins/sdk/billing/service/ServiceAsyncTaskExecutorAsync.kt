package com.appcoins.sdk.billing.service

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

class ServiceAsyncTaskExecutorAsync internal constructor(
    private val bdsService: BdsService,
    private val baseUrl: String,
    private val endPoint: String,
    private val httpMethod: String,
    private val paths: List<String>,
    private val queries: Map<String, String>,
    private val header: Map<String, String>,
    private val body: Map<String, Any>,
    private val serviceResponseListener: ServiceResponseListener?
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
                    body
                )
            handler.post {
                serviceResponseListener?.onResponseReceived(requestResponse)
            }
        }
    }
}
