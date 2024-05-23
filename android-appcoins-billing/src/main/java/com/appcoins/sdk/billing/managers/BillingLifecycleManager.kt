package com.appcoins.sdk.billing.managers

import android.content.Context
import android.content.Intent
import com.appcoins.sdk.billing.service.BillingLifecycleService

class BillingLifecycleManager {
    companion object {
        fun initializeBillingLifecycleService(context: Context) {
            val intent = Intent(context, BillingLifecycleService::class.java)
            context.applicationContext.startService(intent)
        }

        fun stopBillingLifecycleService(context: Context) {
            context.applicationContext.stopService(
                Intent(context, BillingLifecycleService::class.java)
            )
        }
    }
}
