package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.Surface
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod.Companion.SCREEN_ORIENTATION_LANDSCAPE
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod.Companion.SCREEN_ORIENTATION_PORTRAIT
import com.appcoins.sdk.billing.payflow.models.WebViewDetails
import com.appcoins.sdk.core.ui.getScreenRotation

internal object WebViewOrientationUtils {
    fun setupOrientation(activity: Activity, webViewDetails: WebViewDetails?) {
        val forcedOrientation = webViewDetails?.forcedScreenOrientation
        val rotation = getScreenRotation(activity)
        val orientation: Int? =
            when (forcedOrientation) {
                SCREEN_ORIENTATION_PORTRAIT ->
                    if (rotation == Surface.ROTATION_180) {
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }

                SCREEN_ORIENTATION_LANDSCAPE ->
                    if (rotation == Surface.ROTATION_270) {
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }

                else -> null
            }

        orientation?.let {
            activity.requestedOrientation = orientation
        }
    }
}
