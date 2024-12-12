package com.appcoins.sdk.billing.usecases.mmp

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.managers.BrokerManager.getTransaction
import com.appcoins.sdk.billing.managers.MMPEventsManager.sendSuccessfulPurchaseResultEvent
import com.appcoins.sdk.billing.managers.ProductV2Manager
import com.appcoins.sdk.billing.usecases.RetryFailedRequests
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.logger.Logger.logError

object SendSuccessfulPurchaseResponseEvent : UseCase() {

    operator fun invoke(purchase: Purchase) {
        super.invokeUseCase()
        Thread {
            try {
                val purchaseToken = purchase.token ?: throw NullPointerException("Purchase Token is missing.")
                val inappPurchaseResponse = ProductV2Manager.getInappPurchase(purchaseToken)

                val orderId =
                    inappPurchaseResponse?.order?.reference ?: throw NullPointerException("OrderID is missing.")
                val transactionResponse = getTransaction(orderId)

                val price = transactionResponse?.price?.appc ?: handleMissingPrice()
                val paymentMethod = transactionResponse?.method ?: handleMissingPaymentMethod()

                sendSuccessfulPurchaseResultEvent(purchase, orderId, price, paymentMethod)
            } catch (ex: NullPointerException) {
                logError("There was an error creating the Successful Purchase event for MMP.", ex)
            }
            RetryFailedRequests()
        }.start()
    }

    private fun handleMissingPrice(): String {
        logError("There was an error obtaining the Price. Using $DEFAULT_PRICE")
        return DEFAULT_PRICE
    }

    private fun handleMissingPaymentMethod(): String {
        logError("There was an error obtaining the Method. Using $UNKNOWN_PAYMENT_METHOD")
        return UNKNOWN_PAYMENT_METHOD
    }

    private const val DEFAULT_PRICE = "0"
    private const val UNKNOWN_PAYMENT_METHOD = "unknown"
}
