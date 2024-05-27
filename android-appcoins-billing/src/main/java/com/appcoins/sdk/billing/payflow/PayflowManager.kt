package com.appcoins.sdk.billing.payflow

import android.content.Context
import android.content.Intent
import com.appcoins.sdk.billing.service.PayflowSSEService

object PayflowManager {

    @JvmStatic
    fun initializePayflowPrioritySSEClient(context: Context) {
        val intent = Intent(context, PayflowSSEService::class.java)
        context.startService(intent)
    }

    @JvmStatic
    fun stopPayflowPrioritySSEClient(context: Context) {
        context.stopService(Intent(context, PayflowSSEService::class.java))
    }
}
