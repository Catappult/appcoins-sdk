package com.appcoins.sdk.core.logger

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.appcoins.sdk.core.BuildConfig

object Logger {
    private const val TAG = "APPCBillingSDK_" + BuildConfig.VERSION_CODE

    private var isDebuggable = false

    @JvmStatic
    fun setupLogger(context: Context) {
        isDebuggable =
            (context.applicationContext.applicationInfo.flags.and(ApplicationInfo.FLAG_DEBUGGABLE)) != 0
    }

    @JvmStatic
    fun logInfo(message: String) {
        Log.i(TAG, "${getClassAndMethodName()} $message")
    }

    @JvmStatic
    fun logDebug(message: String) {
        if (isDebuggable) {
            Log.d(TAG, "${getClassAndMethodName()} $message")
        }
    }

    @JvmStatic
    fun logVerbose(message: String) {
        Log.v(TAG, "${getClassAndMethodName()} $message")
    }

    @JvmStatic
    fun logWarning(message: String) {
        Log.w(TAG, "${getClassAndMethodName()} $message")
    }

    @JvmStatic
    fun logWarningDebug(message: String) {
        if (isDebuggable) {
            Log.w(TAG, "${getClassAndMethodName()} $message")
        }
    }

    @JvmStatic
    fun logError(message: String, exception: Exception) {
        Log.e(TAG, "${getClassAndMethodName()} $message", exception)
    }

    @JvmStatic
    fun logError(message: String) {
        Log.e(TAG, "${getClassAndMethodName()} $message")
    }

    private fun getClassAndMethodName(): String {
        val stackTrace = Thread.currentThread().stackTrace
        return if (stackTrace.size >= 5) {
            val element = stackTrace[4]
            val className = element.className.substringAfterLast(".")
            val methodName = element.methodName
            "[$className#$methodName]"
        } else {
            "[Unknown]"
        }
    }
}