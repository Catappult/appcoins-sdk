package com.appcoins.sdk.billing.usecases.mmp

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.managers.BrokerManager.getTransaction
import com.appcoins.sdk.billing.managers.MMPEventsManager.sendSuccessfulPurchaseResultEvent
import com.appcoins.sdk.billing.managers.ProductV2Manager

class SendSuccessfulPurchaseResponseEvent {
    companion object {
        fun invoke(purchase: Purchase) =
            Thread {
                val purchaseResponse = purchase.token?.let { ProductV2Manager.getPurchase(it) }
                val transactionResponse =
                    purchaseResponse?.order?.reference?.let { getTransaction(it) }
                sendSuccessfulPurchaseResultEvent(purchase, purchaseResponse?.order?.reference, transactionResponse?.price?.appc)
            }.start()
    }
}
