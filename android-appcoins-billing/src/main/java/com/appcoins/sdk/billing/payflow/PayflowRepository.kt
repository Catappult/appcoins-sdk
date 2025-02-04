package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.usecases.GetQueriesListForPayflowPriority
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType

class PayflowRepository(private val bdsService: BdsService) {

    fun getPayflowPriorityAsync(payflowListener: PayflowListener) {
        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                payflowListener.onResponse(PayflowResponseMapper().map(requestResponse))
            }
        bdsService.makeRequest(
            "/$PAYFLOW_VERSION/payment_flow",
            "GET",
            emptyList(),
            GetQueriesListForPayflowPriority(),
            emptyMap(),
            emptyMap(),
            serviceResponseListener,
            SdkBackendRequestType.PAYMENT_FLOW
        )
    }

    private companion object {
        const val PAYFLOW_VERSION: String = "v2"
    }
}
