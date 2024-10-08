package com.appcoins.sdk.billing.usecases.mmp

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.managers.BrokerManager.getTransaction
import com.appcoins.sdk.billing.managers.MMPEventsManager.sendSuccessfulPurchaseResultEvent
import com.appcoins.sdk.billing.managers.ProductV2Manager
import com.appcoins.sdk.billing.usecases.UseCase

object SendSuccessfulPurchaseResponseEvent : UseCase() {
    operator fun invoke(purchase: Purchase) {
        super.invokeUseCase()
        Thread {
            val purchaseToken = purchase.token ?: return@Thread
            val inappPurchaseResponse = ProductV2Manager.getInappPurchase(purchaseToken)

            val orderId = inappPurchaseResponse?.order?.reference ?: return@Thread
            val transactionResponse = getTransaction(orderId)

            val price = transactionResponse?.price?.appc ?: return@Thread

            sendSuccessfulPurchaseResultEvent(purchase, orderId, price)
        }.start()
    }
}
