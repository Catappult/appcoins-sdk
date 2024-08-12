package com.appcoins.sdk.billing

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.helpers.BindType
import com.appcoins.sdk.billing.helpers.IBinderWalletNotInstalled
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.helpers.WebAppcoinsBilling
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
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
                    val successfullyBound = bindBillingService(context, connection)
                    if (successfullyBound) return
                }

                is PaymentFlowMethod.WebPayment -> {
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
            val successfullyBound = bindBillingService(context, connection)
            if (successfullyBound) return
        }
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

                else -> Unit
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun bindFailedBehaviour(connection: ServiceConnection): Boolean {
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
        serviceIntent: Intent
    ): Boolean =
        if (context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)) {
            logInfo("Binding to the wallet aidl.")
            bindType = BindType.AIDL
            true
        } else {
            bindFailedBehaviour(connection)
        }

    private fun bindBillingService(context: Context, connection: ServiceConnection): Boolean {
        val packageName = WalletUtils.getBillingServicePackageName()
        val iabAction = WalletUtils.getBillingServiceIabAction()
        val serviceIntent = Intent(iabAction)
        serviceIntent.setPackage(packageName)
        return billingServiceInstalledBehaviour(context, connection, serviceIntent)
    }
}
