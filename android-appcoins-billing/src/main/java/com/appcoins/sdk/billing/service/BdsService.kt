package com.appcoins.sdk.billing.service

import com.appcoins.sdk.billing.service.usecases.CreateHttpRequest
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType

class BdsService(val baseUrl: String, val timeoutInMillis: Int) : Service {

    fun createRequest(
        baseUrl: String,
        endPoint: String?,
        httpMethod: String,
        paths: List<String>?,
        queries: Map<String, String>,
        header: Map<String, String>?,
        body: Map<String, Any>?,
        sdkBackendRequestType: SdkBackendRequestType,
    ): RequestResponse =
        CreateHttpRequest(
            baseUrl,
            endPoint,
            httpMethod,
            paths,
            queries,
            header,
            body,
            sdkBackendRequestType,
            timeoutInMillis,
        )

    override fun makeRequest(
        endPoint: String,
        httpMethod: String,
        paths: List<String>?,
        queries: Map<String, String>?,
        header: Map<String, String>,
        body: Map<String, Any>,
        serviceResponseListener: ServiceResponseListener,
        sdkBackendRequestType: SdkBackendRequestType
    ) {
        val serviceAsyncTaskExecutorAsync =
            ServiceAsyncTaskExecutorAsync(
                this,
                baseUrl,
                endPoint,
                httpMethod,
                paths ?: emptyList(),
                queries ?: emptyMap(),
                header,
                body,
                serviceResponseListener,
                sdkBackendRequestType
            )
        serviceAsyncTaskExecutorAsync.execute()
    }

    companion object {
        const val TIME_OUT_IN_MILLIS = 30000
    }
}
