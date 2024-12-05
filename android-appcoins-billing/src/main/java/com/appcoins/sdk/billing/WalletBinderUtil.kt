package com.appcoins.sdk.billing

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.BindType
import com.appcoins.sdk.billing.helpers.IBinderWalletNotInstalled
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.billing.webpayment.WebAppcoinsBilling
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo

object WalletBinderUtil {

    @JvmStatic
    var bindType: BindType? = null
        private set

    @JvmStatic
    fun initializeBillingRepository(
        context: Context,
        connection: ServiceConnection,
        paymentFlowMethods: List<PaymentFlowMethod>?,
    ) {
        paymentFlowMethods?.forEach { paymentFlowMethod ->
            when (paymentFlowMethod) {
                is PaymentFlowMethod.Wallet,
                is PaymentFlowMethod.GamesHub,
                is PaymentFlowMethod.AptoideGames -> {
                    val successfullyBound =
                        bindBillingService(context, connection, paymentFlowMethod)
                    if (successfullyBound) return
                }

                is PaymentFlowMethod.WebPayment -> {
                    logInfo("Creating WebAppcoinsBilling service.")
                    bindType = BindType.BILLING_SERVICE_NOT_INSTALLED
                    connection.onServiceConnected(
                        ComponentName("", WebAppcoinsBilling::class.java.simpleName),
                        IBinderWalletNotInstalled()
                    )
                    return
                }
            }
        }

        if (WalletUtils.hasBillingServiceInstalled()) {
            logInfo("Billing App is installed but not used found in PaymentFlowMethods.")
            val successfullyBound = bindBillingService(context, connection)
            if (successfullyBound) return
        }
        logInfo("Creating WebAppcoinsBilling service as a fallback.")
        bindType = BindType.BILLING_SERVICE_NOT_INSTALLED
        connection.onServiceConnected(
            ComponentName("", WebAppcoinsBilling::class.java.simpleName),
            IBinderWalletNotInstalled()
        )
    }

    @JvmStatic
    fun finishBillingRepository(context: Context, connection: ServiceConnection) {
        try {
            when (bindType) {
                BindType.AIDL -> context.unbindService(connection)
                BindType.BILLING_SERVICE_NOT_INSTALLED ->
                    connection.onServiceDisconnected(
                        ComponentName(context, WebAppcoinsBilling::class.java)
                    )

                BindType.URI_CONNECTION ->
                    connection.onServiceDisconnected(
                        ComponentName(context, UriCommunicationAppcoinsBilling::class.java)
                    )
            }
        } catch (e: IllegalArgumentException) {
            logError("Failed to finish Billing Repository: $e")
        }
    }

    private fun bindFailedBehaviour(
        connection: ServiceConnection,
        paymentFlowMethod: PaymentFlowMethod?
    ): Boolean {
        logError("Failed to Bind to Billing App. Attempting URI Communication Protocol.")
        WalletUtils.getSdkAnalytics()
            .sendCallBindServiceFailEvent(
                paymentFlowMethod?.javaClass?.simpleName ?: "unknown_billing_app",
                paymentFlowMethod?.priority ?: 1
            )
        if (BuildConfig.URI_COMMUNICATION) {
            logInfo("Establishing URI Communication Protocol with Wallet.")
            bindType = BindType.URI_CONNECTION
            connection.onServiceConnected(
                ComponentName("", UriCommunicationAppcoinsBilling::class.java.simpleName),
                IBinderWalletNotInstalled()
            )
            return true
        }
        logInfo("Failed to establish URI Communication Protocol with Wallet.")
        return false
    }

    private fun billingServiceInstalledBehaviour(
        context: Context,
        connection: ServiceConnection,
        serviceIntent: Intent,
        paymentFlowMethod: PaymentFlowMethod?
    ): Boolean =
        if (context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)) {
            logInfo("Binding to the wallet aidl.")
            bindType = BindType.AIDL
            true
        } else {
            bindFailedBehaviour(connection, paymentFlowMethod)
        }

    private fun bindBillingService(
        context: Context,
        connection: ServiceConnection,
        paymentFlowMethod: PaymentFlowMethod? = null
    ): Boolean {
        logInfo("Attempting to bind to a Billing App: ${paymentFlowMethod?.name}")
        val packageName = WalletUtils.getBillingServicePackageName()
        val iabAction = WalletUtils.getBillingServiceIabAction()
        val serviceIntent = Intent(iabAction)
        serviceIntent.setPackage(packageName)
        return billingServiceInstalledBehaviour(
            context,
            connection,
            serviceIntent,
            paymentFlowMethod
        )
    }
}
