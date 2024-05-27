package com.appcoins.sdk.billing.payflow

import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.listeners.PayflowPriorityStream
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.utils.ServiceUtils

object PayflowManager {

    private val payflowRepository by lazy {
        PayflowRepository(BdsService(BuildConfig.PAYFLOW_HOST, 30000))
    }

    @JvmStatic
    fun getPayflowPriorityAsync() {
        val payflowListener = object : PayflowListener {
            override fun onResponse(payflowMethodResponse: PayflowMethodResponse) {
                payflowMethodResponse.responseCode?.let { responseCode ->
                    if (ServiceUtils.isSuccess(responseCode)) {
                        val sortedMethods =
                            payflowMethodResponse.paymentFlowList?.sortedBy { it.priority }
                        PayflowPriorityStream.getInstance().emit(sortedMethods)
                    } else {
                        PayflowPriorityStream.getInstance().emit(null)
                    }
                }
            }
        }

        payflowRepository.getPayflowPriorityAsync(payflowListener)
    }
}
