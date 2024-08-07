package com.appcoins.sdk.billing.mappers

import android.os.Bundle
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.listeners.PurchasesModel
import com.appcoins.sdk.billing.managers.WalletManager.requestWallet
import com.appcoins.sdk.billing.repositories.BrokerRepository
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ID_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ITEM_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE

internal class PurchasesBundleMapper(private val brokerRepository: BrokerRepository) {
    fun mapGuestPurchases(
        bundleResponse: Bundle,
        walletId: String?,
        packageName: String,
        type: String,
        idsList: ArrayList<String>? = ArrayList(),
        skuList: ArrayList<String>? = ArrayList(),
        dataList: ArrayList<String>? = ArrayList(),
        signatureDataList: ArrayList<String>? = ArrayList()
    ): Bundle {
        val walletGenerationModel = requestWallet(walletId!!)

        val purchasesModel =
            brokerRepository.getPurchasesSync(
                packageName,
                walletGenerationModel.walletAddress,
                walletGenerationModel.signature,
                type
            )

        buildPurchaseBundle(
            bundleResponse,
            purchasesModel,
            idsList ?: ArrayList(),
            skuList ?: ArrayList(),
            dataList ?: ArrayList(),
            signatureDataList ?: ArrayList()
        )

        return bundleResponse
    }

    private fun buildPurchaseBundle(
        bundle: Bundle,
        purchasesModel: PurchasesModel,
        idsList: ArrayList<String>,
        skuList: ArrayList<String>,
        dataList: ArrayList<String>,
        signatureDataList: ArrayList<String>
    ) {
        for (skuPurchase in purchasesModel.skuPurchases) {
            idsList.add(skuPurchase.uid)
            dataList.add(skuPurchase.signature.message.toString())
            signatureDataList.add(skuPurchase.signature.value)
            skuList.add(skuPurchase.product.name)
        }
        bundle.putInt(RESPONSE_CODE, ResponseCode.OK.value)
        bundle.putStringArrayList(INAPP_PURCHASE_ID_LIST, idsList)
        bundle.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, skuList)
        bundle.putStringArrayList(INAPP_PURCHASE_DATA_LIST, dataList)
        bundle.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, signatureDataList)
    }
}
