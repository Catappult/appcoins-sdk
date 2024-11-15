package com.appcoins.sdk.billing.usecases.mmp

import com.appcoins.sdk.billing.Purchase
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.managers.BrokerManager.getTransaction
import com.appcoins.sdk.billing.managers.MMPEventsManager.sendSuccessfulPurchaseResultEvent
import com.appcoins.sdk.billing.managers.ProductV2Manager
import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.billing.usecases.RetryFailedRequests
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.logger.Logger.logError

object SendSuccessfulPurchaseResponseEvent : UseCase() {

    private val backendRequestsSharedPreferences: BackendRequestsSharedPreferences by lazy {
        BackendRequestsSharedPreferences(WalletUtils.context)
    }

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

                sendSuccessfulPurchaseResultEvent(purchase, orderId, price)
            } catch (ex: NullPointerException) {
                logError("There was an error creating the Successful Purchase event for MMP.", ex)
            }
            RetryFailedRequests(backendRequestsSharedPreferences)
        }.start()
    }

    private fun handleMissingPrice(): String {
        logError("There was an error obtaining the Price. Using 0")
        return "0"
    }
}
