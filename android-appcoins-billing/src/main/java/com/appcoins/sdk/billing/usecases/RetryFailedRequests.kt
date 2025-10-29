package com.appcoins.sdk.billing.usecases

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.RequestData
import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logDebug

object RetryFailedRequests : UseCase() {

    private val backendRequestsSharedPreferences: BackendRequestsSharedPreferences by lazy {
        BackendRequestsSharedPreferences(WalletUtils.context)
    }

    operator fun invoke() {
        super.invokeUseCase()

        val failedRequests = backendRequestsSharedPreferences.getFailedRequests()
        val blacklistFilteredRequests = filterBlacklistedEndpoints(failedRequests)

        blacklistFilteredRequests?.iterator()?.let { iterator ->
            if (failedRequests?.size != blacklistFilteredRequests.size) {
                backendRequestsSharedPreferences.setFailedRequests(blacklistFilteredRequests)
            }
            while (iterator.hasNext()) {
                val request = iterator.next()
                val newBdsService = BdsService(request.baseUrl, request.timeoutInMillis)
                newBdsService.makeRequest(
                    request.endPoint ?: "",
                    request.httpMethod,
                    request.paths,
                    request.queries,
                    request.header,
                    request.body,
                    { requestResponse ->
                        if (isSuccess(requestResponse.responseCode)) {
                            blacklistFilteredRequests.remove(request)
                            backendRequestsSharedPreferences.setFailedRequests(blacklistFilteredRequests)
                        }
                    },
                    request.sdkBackendRequestType
                )
            }
        }
    }

    private fun filterBlacklistedEndpoints(failedRequests: List<RequestData>?): MutableList<RequestData>? {
        return failedRequests?.filter {
            logDebug("Checking endpoint: ${it.baseUrl + it.endPoint}")
            (!blacklistedUrls.contains(it.baseUrl + it.endPoint)).apply {
                if (!this) {
                    logDebug("Blacklisted endpoint: ${it.baseUrl + it.endPoint}")
                }
            }
        }?.toMutableList()
    }

    private val blacklistedUrls = listOf(
        BuildConfig.MMP_BASE_HOST + "/session_start",
        BuildConfig.MMP_BASE_HOST + "/session_end",
    )
}
