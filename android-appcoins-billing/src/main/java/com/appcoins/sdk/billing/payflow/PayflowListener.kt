package com.appcoins.sdk.billing.payflow

interface PayflowListener {
  fun onResponse(payflowList: List<PaymentFlowMethod>?)
}
