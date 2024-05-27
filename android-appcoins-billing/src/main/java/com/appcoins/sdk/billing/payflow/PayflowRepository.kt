package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.usecases.GetQueriesListForPayflowPriority

class PayflowRepository(private val bdsService: BdsService) {

    fun getPayflowPriorityAsync(payflowListener: PayflowListener) {
        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                payflowListener.onResponse(PayflowResponseMapper().map(requestResponse))
            }
        bdsService.makeRequest(
            "/payment_flow",
            "GET",
            emptyList(),
            GetQueriesListForPayflowPriority.invoke(),
            emptyMap(),
            emptyMap(),
            serviceResponseListener
        )
    }
}