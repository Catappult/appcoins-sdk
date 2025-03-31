package com.appcoins.sdk.billing.service

import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType

class BdsRetryService(
    private val bdsService: BdsService,
    private val sharedPreferences: BackendRequestsSharedPreferences
) : Service {

    override fun makeRequest(
        endPoint: String,
        httpMethod: String,
        paths: List<String>,
        queries: Map<String, String>,
        header: Map<String, String>,
        body: Map<String, Any>,
        serviceResponseListener: ServiceResponseListener?,
        sdkBackendRequestType: SdkBackendRequestType
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
                            body
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
        body: Map<String, Any>
    ) {
        val failedRequests = getFailedRequests()?.toMutableList() ?: mutableListOf()
        val requestData = RequestData(
            sdkBackendRequestType,
            baseUrl,
            timeoutInMillis,
            endPoint,
            httpMethod,
            paths,
            queries,
            header,
            body
        )
        failedRequests.add(requestData)
        sharedPreferences.setFailedRequests(failedRequests)
    }

    private fun getFailedRequests(): List<RequestData>? {
        return sharedPreferences.getFailedRequests()
    }
}
