package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess

object RetryFailedRequests : UseCase() {

    private val backendRequestsSharedPreferences: BackendRequestsSharedPreferences by lazy {
        BackendRequestsSharedPreferences(WalletUtils.context)
    }

    operator fun invoke() {
        super.invokeUseCase()

        val failedRequests = backendRequestsSharedPreferences.getFailedRequests()?.toMutableList()

        failedRequests?.iterator()?.let { iterator ->
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
                            failedRequests.remove(request)
                            backendRequestsSharedPreferences.setFailedRequests(failedRequests)
                        }
                    },
                    request.sdkBackendRequestType
                )
            }
        }
    }
}
