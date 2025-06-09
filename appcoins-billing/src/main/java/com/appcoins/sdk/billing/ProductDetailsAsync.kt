package com.appcoins.sdk.billing

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException
import com.appcoins.sdk.billing.helpers.AnalyticsMappingHelper
import com.appcoins.sdk.billing.helpers.ProductDetailsMapper
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils.sdkAnalytics
import com.appcoins.sdk.core.logger.Logger.logError

class ProductDetailsAsync(
    private val queryProductDetailsParams: QueryProductDetailsParams,
    private val productDetailsResponseListener: ProductDetailsResponseListener,
    private val repository: Repository
) : Runnable {
    override fun run() {
        try {
            val response = skuDetails

            sdkAnalytics.sendQuerySkuDetailsResult(AnalyticsMappingHelper().mapSkuDetailsToListOfStrings(response))

            productDetailsResponseListener.onProductDetailsResponse(
                BillingResult.Builder().setResponseCode(response.responseCode).build(),
                ProductDetailsMapper().mapSkuDetailsToProductDetails(response.skuDetailsList)
            )
        } catch (e: ServiceConnectionException) {
            logError("Service is not ready to request SkuDetails: $e")
            productDetailsResponseListener.onProductDetailsResponse(
                BillingResult.Builder().setResponseCode(ResponseCode.SERVICE_UNAVAILABLE.value).build(),
                ArrayList()
            )
            sdkAnalytics.sendQuerySkuDetailsResult(null)
        }
    }

    @get:Throws(ServiceConnectionException::class)
    private val skuDetails: SkuDetailsResult
        get() = repository.querySkuDetailsAsync(getProductTypeFromParams(), getProductsListFromParams())

    private fun getProductTypeFromParams(): String {
        return queryProductDetailsParams.productList.firstOrNull()?.productType ?: "inapp"
    }

    private fun getProductsListFromParams(): List<String> {
        return queryProductDetailsParams.productList.map { it.productId }
    }
}
