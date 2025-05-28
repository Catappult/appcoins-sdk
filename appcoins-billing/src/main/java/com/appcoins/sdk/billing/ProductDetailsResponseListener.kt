package com.appcoins.sdk.billing

fun interface ProductDetailsResponseListener {
    fun onProductDetailsResponse(billingResult: BillingResult, details: List<ProductDetails>): Unit
}
