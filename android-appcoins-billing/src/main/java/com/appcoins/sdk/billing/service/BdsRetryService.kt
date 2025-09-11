package com.appcoins.sdk.billing.service

import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import java.util.concurrent.TimeUnit

class BdsRetryService(
    private val bdsService: BdsService,
    private val sharedPreferences: BackendRequestsSharedPreferences
) : RetryService {

    override fun makeRequest(
        endPoint: String,
        httpMethod: String,
        paths: List<String>,
        queries: Map<String, String>,
        header: Map<String, String>,
        body: Map<String, Any>,
        serviceResponseListener: ServiceResponseListener?,
        sdkBackendRequestType: SdkBackendRequestType,
        timestamp: Long,
    ) {
        val serviceAsyncTaskExecutorAsync =
            ServiceAsyncTaskExecutorAsync(
                bdsService,
                bdsService.baseUrl,
                endPoint,
                httpMethod,
                paths.toMutableList(),
                queries.toMutableMap(),
                header.toMutableMap(),
                body.toMutableMap(),
                serviceResponseListener = {
                    serviceResponseListener?.onResponseReceived(it)
                    if (!isSuccess(it.responseCode)) {
                        saveFailedRequest(
                            sdkBackendRequestType,
                            bdsService.baseUrl,
                            bdsService.timeoutInMillis,
                            endPoint,
                            httpMethod,
                            paths,
                            queries,
                            header,
                            body,
                            timestamp
                        )
                    }
                },
                sdkBackendRequestType = sdkBackendRequestType,
            )
        serviceAsyncTaskExecutorAsync.execute()
    }

    private fun saveFailedRequest(
        sdkBackendRequestType: SdkBackendRequestType,
        baseUrl: String,
        timeoutInMillis: Int,
        endPoint: String?,
        httpMethod: String,
        paths: List<String>,
        queries: Map<String, String>,
        header: Map<String, String>,
        body: Map<String, Any>,
        timestamp: Long,
    ) {
        val requestData = RequestData(
            sdkBackendRequestType,
            baseUrl,
            timeoutInMillis,
            endPoint,
            httpMethod,
            paths,
            queries,
            header,
            body,
            timestamp,
        )

        val failedRequests = getFailedRequests()?.toMutableList() ?: mutableListOf()
        failedRequests.add(requestData)

        val cleanedRequests = filterAdmissibleRequests(failedRequests)

        sharedPreferences.setFailedRequests(cleanedRequests)
    }

    private fun getFailedRequests(): List<RequestData>? {
        return sharedPreferences.getFailedRequests()
    }

    private fun filterAdmissibleRequests(
        requests: List<RequestData>
    ): List<RequestData> {
        val nowTimestamp = System.currentTimeMillis()
        val thirtyDaysMillis = TimeUnit.DAYS.toMillis(TIMEOUT_30_SECS)

        return requests
            .filter { nowTimestamp - it.timestamp <= thirtyDaysMillis }
            .groupBy { it.sdkBackendRequestType }
            .flatMap { (_, requests) ->
                requests.sortedByDescending { it.timestamp }.take(MAX_FAILED_REQUESTS)
            }
    }

    private companion object {
        const val TIMEOUT_30_SECS = 30L
        const val MAX_FAILED_REQUESTS = 100
    }
}
