package com.appcoins.sdk.billing.usecases.mmp

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.managers.BrokerManager.getTransaction
import com.appcoins.sdk.billing.managers.MMPEventsManager.sendSuccessfulPurchaseResultEvent
import com.appcoins.sdk.billing.managers.ProductV2Manager

class SendSuccessfulPurchaseResponseEvent {
    companion object {
        fun invoke(purchase: Purchase) =
            Thread {
                val purchaseToken = purchase.token ?: return@Thread
                val purchaseResponse = ProductV2Manager.getPurchase(purchaseToken)

                val orderId = purchaseResponse?.order?.reference ?: return@Thread
                val transactionResponse = getTransaction(orderId)

                val price = transactionResponse?.price?.appc ?: return@Thread

                sendSuccessfulPurchaseResultEvent(purchase, orderId, price)
            }.start()
    }
}
