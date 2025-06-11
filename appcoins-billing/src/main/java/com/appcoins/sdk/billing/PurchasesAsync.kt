package com.appcoins.sdk.billing

import android.util.Base64
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException
import com.appcoins.sdk.billing.helpers.AnalyticsMappingHelper
import com.appcoins.sdk.billing.helpers.BillingResultHelper
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

            if (purchasesResult.billingResult.responseCode != ResponseCode.OK.value) {
                purchasesResponseListener.onQueryPurchasesResponse(
                    purchasesResult.billingResult,
                    emptyList()
                )
                sdkAnalytics.sendQueryPurchasesResultEvent(null)
                return
            }

            for (purchase in purchasesResult.purchasesList) {
                val purchaseData = purchase.originalJson
                val decodeSignature = purchase.signature

                if (!PurchasesSecurityHelper.verifyPurchase(
                        purchaseData,
                        Base64.decode(decodeSignature, Base64.DEFAULT)
                    )
                ) {
                    purchasesResponseListener.onQueryPurchasesResponse(
                        BillingResultHelper.buildBillingResult(
                            ResponseCode.DEVELOPER_ERROR.value,
                            BillingResultHelper.ERROR_TYPE_INVALID_PUBLIC_KEY
                        ),
                        emptyList()
                    )
                    sdkAnalytics.sendQueryPurchasesResultEvent(null)
                    return
                }
            }

            purchasesResponseListener.onQueryPurchasesResponse(
                purchasesResult.billingResult,
                purchasesResult.purchasesList
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
