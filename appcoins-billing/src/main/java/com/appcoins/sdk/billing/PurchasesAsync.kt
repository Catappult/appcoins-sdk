package com.appcoins.sdk.billing

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException
import com.appcoins.sdk.billing.helpers.AnalyticsMappingHelper
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils.sdkAnalytics
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.security.PurchasesSecurityHelper

class PurchasesAsync(
    private val queryPurchasesParams: QueryPurchasesParams,
    private val purchasesResponseListener: PurchasesResponseListener,
    private val repository: Repository
) : Runnable {
    override fun run() {
        try {
            val purchasesResult = purchases

            if (purchasesResult.responseCode != ResponseCode.OK.value) {
                purchasesResponseListener.onQueryPurchasesResponse(
                    BillingResult(purchasesResult.responseCode),
                    emptyList()
                )
                sdkAnalytics.sendQueryPurchasesResultEvent(null)
                return
            }

            for (purchase in purchasesResult.purchases) {
                val purchaseData = purchase.originalJson
                val decodeSignature = purchase.signature

                if (!PurchasesSecurityHelper.verifyPurchase(purchaseData, decodeSignature)) {
                    purchasesResponseListener.onQueryPurchasesResponse(
                        BillingResult(
                            ResponseCode.ERROR.value,
                            "Signature verification failed. Verify the Public Key used in the Billing SDK Initialization and ensure it is the correct one."
                        ),
                        emptyList()
                    )
                    sdkAnalytics.sendQueryPurchasesResultEvent(null)
                    return
                }
            }

            purchasesResponseListener.onQueryPurchasesResponse(
                BillingResult(purchasesResult.responseCode),
                purchasesResult.purchases
            )
            sdkAnalytics.sendQueryPurchasesResultEvent(
                AnalyticsMappingHelper().mapPurchasesToListOfStrings(purchasesResult)
            )
            return
        } catch (e: ServiceConnectionException) {
            logError("Service is not ready to request Purchases: $e")
            purchasesResponseListener.onQueryPurchasesResponse(
                BillingResult(ResponseCode.SERVICE_UNAVAILABLE.value),
                emptyList()
            )
            sdkAnalytics.sendQueryPurchasesResultEvent(null)
            return
        }

    }

    @get:Throws(ServiceConnectionException::class)
    private val purchases: PurchasesResult
        get() = repository.getPurchases(queryPurchasesParams.productType)
}
