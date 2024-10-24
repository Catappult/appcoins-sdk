package com.appcoins.sdk.billing.mappers

import android.os.Bundle
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_VOIDED_PURCHASE_DATA_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE

internal class VoidedPurchasesBundleMapper {
    fun mapGuestVoidedPurchases(
        bundleResponse: Bundle,
        voidedPurchasesResponse: VoidedPurchasesResponse,
        dataList: ArrayList<String>? = ArrayList(),
    ): Bundle {
        buildPurchaseBundle(
            bundleResponse,
            voidedPurchasesResponse,
            dataList ?: ArrayList(),
        )

        return bundleResponse
    }

    private fun buildPurchaseBundle(
        bundle: Bundle,
        voidedPurchasesResponse: VoidedPurchasesResponse,
        dataList: ArrayList<String>,
    ) {
        for (voidedPurchase in voidedPurchasesResponse.voidedPurchases) {
            dataList.add(voidedPurchase.toJson())
        }
        bundle.putInt(RESPONSE_CODE, ResponseCode.OK.value)
        bundle.putStringArrayList(INAPP_VOIDED_PURCHASE_DATA_LIST, dataList)
    }
}
