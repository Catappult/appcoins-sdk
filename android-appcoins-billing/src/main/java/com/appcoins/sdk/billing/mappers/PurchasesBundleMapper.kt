package com.appcoins.sdk.billing.mappers

import android.os.Bundle
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ID_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ITEM_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE

internal class PurchasesBundleMapper {
    fun mapGuestPurchases(
        bundleResponse: Bundle,
        purchasesResponse: PurchasesResponse,
        idsList: ArrayList<String>? = ArrayList(),
        skuList: ArrayList<String>? = ArrayList(),
        dataList: ArrayList<String>? = ArrayList(),
        signatureDataList: ArrayList<String>? = ArrayList()
    ): Bundle {
        buildPurchaseBundle(
            bundleResponse,
            purchasesResponse,
            idsList ?: ArrayList(),
            skuList ?: ArrayList(),
            dataList ?: ArrayList(),
            signatureDataList ?: ArrayList()
        )

        return bundleResponse
    }

    private fun buildPurchaseBundle(
        bundle: Bundle,
        purchasesResponse: PurchasesResponse,
        idsList: ArrayList<String>,
        skuList: ArrayList<String>,
        dataList: ArrayList<String>,
        signatureDataList: ArrayList<String>
    ) {
        for (skuPurchase in purchasesResponse.purchases) {
            idsList.add(skuPurchase.uid)
            dataList.add(skuPurchase.verification.data)
            signatureDataList.add(skuPurchase.verification.signature)
            skuList.add(skuPurchase.sku)
        }
        bundle.putInt(RESPONSE_CODE, ResponseCode.OK.value)
        bundle.putStringArrayList(INAPP_PURCHASE_ID_LIST, idsList)
        bundle.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, skuList)
        bundle.putStringArrayList(INAPP_PURCHASE_DATA_LIST, dataList)
        bundle.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, signatureDataList)
    }
}
