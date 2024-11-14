package com.appcoins.sdk.billing.usecases

import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess

object RetryFailedRequests : UseCase() {
    operator fun invoke(sharedPreferences: BackendRequestsSharedPreferences) {
        super.invokeUseCase()

        val failedRequests = sharedPreferences.getFailedRequests()?.toMutableList()

        failedRequests?.iterator()?.let { iterator ->
            while (iterator.hasNext()) {
                val request = iterator.next()
                val newBdsService = BdsService(request.baseUrl, request.timeoutInMillis)
                newBdsService.makeRequest(
                    request.endPoint,
                    request.httpMethod,
                    request.paths,
                    request.queries,
                    request.header,
                    request.body
                ) { requestResponse ->
                    if (isSuccess(requestResponse.responseCode)) {
                        failedRequests.remove(request)
                        sharedPreferences.setFailedRequests(failedRequests)
                    }
                }
            }
        }
    }
}
