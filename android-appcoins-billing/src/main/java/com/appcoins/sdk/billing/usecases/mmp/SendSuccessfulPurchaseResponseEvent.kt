package com.appcoins.sdk.billing.usecases.mmp

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.managers.BrokerManager.getTransaction
import com.appcoins.sdk.billing.managers.MMPEventsManager.sendSuccessfulPurchaseResultEvent

class SendSuccessfulPurchaseResponseEvent {
    companion object {
        fun invoke(purchase: Purchase) =
            Thread {
                val transactionResponse = getTransaction(purchase.orderId)
                sendSuccessfulPurchaseResultEvent(purchase, transactionResponse?.price?.appc)
            }.start()
    }
}
