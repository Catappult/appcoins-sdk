package com.appcoins.sdk.billing.payflow

interface PayflowListener {
    fun onResponse(payflowMethodResponse: PayflowMethodResponse)
}
