package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.payflow.models.PayflowMethodResponse

interface PayflowListener {
    fun onResponse(payflowMethodResponse: PayflowMethodResponse)
}
