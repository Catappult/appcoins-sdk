package com.appcoins.sdk.core.threading

import com.appcoins.sdk.core.logger.Logger.logDebug

class LifecycleAwareThread(
    private val delayInSeconds: Int,
    private val task: () -> Unit
) {

    private var thread: Thread? = null

    @Volatile
    private var isCancelled = false
    private var hasStarted = false

    fun start() {
        try {
            if (hasStarted) return
            hasStarted = true

            thread = Thread {
                try {
                    Thread.sleep(delayInSeconds * MILLISECONDS_IN_SECOND)
                    if (!isCancelled) {
                        task()
                    }
                } catch (e: InterruptedException) {
                    logDebug("Thread was interrupted.")
                }
            }
            thread?.start()
        } catch (ex: Exception) {
            logDebug(ex.toString())
        }
    }

    fun cancel() {
        try {
            isCancelled = true
            thread?.interrupt()
        } catch (ex: Exception) {
            logDebug(ex.toString())
        }
    }

    private companion object {
        const val MILLISECONDS_IN_SECOND = 1000L
    }
}
