package com.appcoins.sdk.ingameupdates.api

import android.content.Context
import android.os.Looper
import com.appcoins.sdk.ingameupdates.usecases.IsUpdateAvailable
import com.appcoins.sdk.ingameupdates.usecases.LaunchAppUpdate

class InGameUpdatesClientImpl(private val context: Context) : InGameUpdatesClient {
    /**
     * Can't be started on Main Thread
     */
    override fun isAppUpdateAvailable(): Boolean =
        if (Looper.myLooper() == Looper.getMainLooper()) {
            false
        } else {
            IsUpdateAvailable.invoke(context)
        }

    override fun launchAppUpdateFlow() {
        Thread {
            if (isAppUpdateAvailable()) {
                LaunchAppUpdate.invoke(context)
            }
        }.start()
    }
}
