package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.service.BdsRetryService
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType

class MMPEventsRepository(private val bdsRetryService: BdsRetryService) {

    fun sendSuccessfulPurchaseResultEvent(
        packageName: String,
        oemId: String?,
        guestWalletId: String,
        sku: String,
        orderId: String,
        purchaseAmount: String,
        paymentMethod: String,
        utmSource: String?,
        utmMedium: String?,
        utmCampaign: String?,
        utmTerm: String?,
        utmContent: String?,
    ) {
        val queries: MutableMap<String, String> = LinkedHashMap()
        queries["package_name"] = packageName
        oemId?.let { queries["oemid"] = it }
        queries["guest_uid"] = guestWalletId
        queries["sku"] = sku
        queries["order_id"] = orderId
        queries["purchase_amount"] = purchaseAmount
        queries["payment_method"] = paymentMethod
        queries["timestamp"] = System.currentTimeMillis().toString()
        utmSource?.let { queries["utm_source"] = it }
        utmMedium?.let { queries["utm_medium"] = it }
        utmCampaign?.let { queries["utm_campaign"] = it }
        utmTerm?.let { queries["utm_term"] = it }
        utmContent?.let { queries["utm_content"] = it }

        bdsRetryService.makeRequest(
            "/purchase",
            "GET",
            mutableListOf(),
            queries,
            mutableMapOf(),
            mutableMapOf(),
            null,
            SdkBackendRequestType.PURCHASE_RESULT_EVENT
        )
    }
}
