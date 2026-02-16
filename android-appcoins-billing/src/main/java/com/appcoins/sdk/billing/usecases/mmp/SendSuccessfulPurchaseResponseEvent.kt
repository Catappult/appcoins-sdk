package com.appcoins.sdk.billing.usecases.mmp

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.managers.BrokerManager.getTransaction
import com.appcoins.sdk.billing.managers.MMPEventsManager.sendSuccessfulPurchaseResultEvent
import com.appcoins.sdk.billing.managers.ProductV2Manager
import com.appcoins.sdk.billing.mappers.Transaction
import com.appcoins.sdk.billing.usecases.RetryFailedRequests
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.date.parseIsoToMillis
import com.appcoins.sdk.core.logger.Logger.logError

object SendSuccessfulPurchaseResponseEvent : UseCase() {

    operator fun invoke(purchase: Purchase? = null, transaction: Transaction? = null) {
        super.invokeUseCase()
        Thread {
            if (purchase != null) {
                handlePurchaseModelEvent(purchase)
            } else if (transaction != null) {
                handleTransactionModelEvent(transaction)
            } else {
                logError("Purchase was not defined in SendSuccessfulPurchaseResponseEvent.")
            }
            RetryFailedRequests()
        }.start()
    }

    private fun handlePurchaseModelEvent(purchase: Purchase) {
        try {
            val purchaseToken = purchase.token ?: throw NullPointerException("PurchaseToken is missing.")
            val timestamp = purchase.purchaseTime
            val inappPurchaseResponse = ProductV2Manager.getInappPurchase(purchaseToken)

            val orderId =
                inappPurchaseResponse?.order?.reference ?: throw NullPointerException("OrderID is missing.")
            val transactionResponse = getTransaction(orderId)

            val price = transactionResponse?.transaction?.price?.appc ?: handleMissingPrice()
            val paymentMethod = transactionResponse?.transaction?.method ?: handleMissingPaymentMethod()

            sendSuccessfulPurchaseResultEvent(purchase.sku, orderId, price, paymentMethod, timestamp)
        } catch (ex: NullPointerException) {
            logError("There was an error creating the Successful Purchase event for MMP.", ex)
        }
    }

    private fun handleTransactionModelEvent(transaction: Transaction) {
        try {
            val productId = transaction.product ?: throw NullPointerException("ProductID is missing.")
            val orderId = transaction.uid ?: throw NullPointerException("OrderID is missing.")

            val price = transaction.price?.appc ?: handleMissingPrice()
            val paymentMethod = transaction.method ?: handleMissingPaymentMethod()
            val timestamp = parseIsoToMillis(transaction.added)

            sendSuccessfulPurchaseResultEvent(productId, orderId, price, paymentMethod, timestamp)
        } catch (ex: NullPointerException) {
            logError("There was an error creating the Successful Purchase event for MMP.", ex)
        }
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
