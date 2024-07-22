package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.service.BdsService

class MMPEventsRepository(private val bdsService: BdsService) {

    fun sendSuccessfulPurchaseResultEvent(
        packageName: String,
        oemId: String?,
        guestWalletId: String?,
        sku: String?,
        orderId: String?,
        purchaseAmount: String?,
        utmSource: String?,
        utmMedium: String?,
        utmCampaign: String?,
        utmTerm: String?,
        utmContent: String?,
    ) {
        val queries: MutableMap<String, String> = LinkedHashMap()
        queries["package_name"] = packageName
        oemId?.let { queries["oemid"] = it }
        guestWalletId?.let { queries["guest_uid"] = it }
        sku?.let { queries["sku"] = it }
        orderId?.let { queries["order_id"] = it }
        purchaseAmount?.let { queries["purchase_amount"] = it }
        utmSource?.let { queries["utm_source"] = it }
        utmMedium?.let { queries["utm_medium"] = it }
        utmCampaign?.let { queries["utm_campaign"] = it }
        utmTerm?.let { queries["utm_term"] = it }
        utmContent?.let { queries["utm_content"] = it }

        bdsService.makeRequest(
            "/attribution",
            "GET",
            emptyList(),
            queries,
            emptyMap(),
            emptyMap(),
            null
        )
    }
}