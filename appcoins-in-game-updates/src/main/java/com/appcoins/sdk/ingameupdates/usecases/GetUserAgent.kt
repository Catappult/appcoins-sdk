package com.appcoins.sdk.ingameupdates.usecases

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.appcoins.sdk.ingameupdates.BuildConfig

object GetUserAgent {

    private lateinit var userAgent: String

    fun invoke(context: Context): String {
        return try {
            userAgent
        } catch (ex: Exception) {
            val displayMetrics = getDisplayMetrics(context)
            var widthPixels = 0
            var heightPixels = 0
            if (displayMetrics != null) {
                widthPixels = displayMetrics.widthPixels
                heightPixels = displayMetrics.heightPixels
            }
            userAgent = buildUserAgent(context, widthPixels, heightPixels)
            userAgent
        }
    }

    private fun buildUserAgent(context: Context, widthPixels: Int, heightPixels: Int): String {
        return ("AppCoinsGuestSDK/"
                + BuildConfig.VERSION_NAME
                + " (Linux; Android "
                + Build.VERSION.RELEASE.replace(";".toRegex(), " ")
                + "; "
                + Build.VERSION.SDK_INT
                + "; "
                + Build.MODEL.replace(";".toRegex(), " ")
                + " Build/"
                + Build.PRODUCT.replace(";", " ")
                + "; "
                + System.getProperty("os.arch")
                + "; "
                + context.packageName
                + "; "
                + BuildConfig.VERSION_CODE
                + "; "
                + widthPixels
                + "x"
                + heightPixels
                + ")")
    }

    private fun getDisplayMetrics(context: Context): DisplayMetrics? {
        val wm =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(displayMetrics)
        } else {
            display.getMetrics(displayMetrics)
        }
        return displayMetrics
    }
}
