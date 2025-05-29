package com.appcoins.sdk.billing

fun interface PurchasesResponseListener {
    fun onQueryPurchasesResponse(billingResult: BillingResult, purchases: List<Purchase>)
}
