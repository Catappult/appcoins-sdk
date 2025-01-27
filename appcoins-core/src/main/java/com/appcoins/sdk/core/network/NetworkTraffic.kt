package com.appcoins.sdk.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.logger.Logger.logWarning


class NetworkTraffic {

    fun getAverageSpeed(context: Context): String? {
        logInfo("Obtaining Network speed information.")
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    val networkCapabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    val downstreamSpeedInMbps = networkCapabilities!!.linkDownstreamBandwidthKbps / 1000

                    logInfo("Network speed obtained: $downstreamSpeedInMbps")
                    downstreamSpeedInMbps.toString()
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    val activeNetwork = connectivityManager.activeNetworkInfo
                    if (activeNetwork != null && activeNetwork.isConnected) {
                        logWarning("Device network information is not available.")
                        "-1"
                    } else {
                        logWarning("Network is not available.")
                        "0"
                    }
                }

                else -> {
                    logWarning("Device not compatible for Network speed evaluation.")
                    null
                }
            }
        } catch (ex: Exception) {
            logWarning("There was an error obtaining network speed. ${ex.message}")
            return null
        }
    }
}
