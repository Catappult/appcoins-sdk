package com.appcoins.sdk.billing.service

import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess

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
        serviceResponseListener: ServiceResponseListener?
    ) {
        val serviceAsyncTaskExecutorAsync =
            ServiceAsyncTaskExecutorAsync(
                bdsService,
                bdsService.baseUrl,
                endPoint,
                httpMethod,
                paths,
                queries,
                header,
                body
            ) {
                serviceResponseListener?.onResponseReceived(it)
                if (!isSuccess(it.responseCode)) {
                    saveFailedRequest(
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
            }
        serviceAsyncTaskExecutorAsync.execute()
    }

    private fun saveFailedRequest(
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
        val requestData = RequestData(baseUrl, timeoutInMillis, endPoint, httpMethod, paths, queries, header, body)
        failedRequests.add(requestData)
        sharedPreferences.setFailedRequests(failedRequests)
    }

    private fun getFailedRequests(): List<RequestData>? {
        return sharedPreferences.getFailedRequests()
    }
}
