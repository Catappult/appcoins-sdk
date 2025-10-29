package com.appcoins.sdk.billing.managers

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.sharedpreferences.UserSharedPreferences
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class SessionManager {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var updater: ScheduledFuture<*>? = null

    private val userSharedPreferences by lazy {
        UserSharedPreferences(WalletUtils.context)
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager().also { INSTANCE = it }
            }
        }

        private const val SESSION_DURATION_SECONDS = 10L
        private const val MILLIS_MULTIPLIER = 1000L
    }

    fun initSession() {
        try {
            stopPreviousUpdater()
            sendPastUserSession()
            initializeNewSession()
            createNewUpdater()
        } catch (e: Exception) {
            logError("Error in session manager.", e)
        }
    }

    private fun stopPreviousUpdater() {
        try {
            updater?.cancel(true)
        } catch (e: Exception) {
            logError("Error stopping updater", e)
        }
    }

    private fun sendPastUserSession() {
        val sessionEndTime = userSharedPreferences.getSessionEndTime().takeIf { it != 0L }
        val sessionStartTime = userSharedPreferences.getSessionStartTime().takeIf { it != 0L }

        if (sessionEndTime != null && sessionStartTime != null) {
            try {
                val sessionDuration = sessionEndTime - sessionStartTime
                logDebug("Sending previous session. Session duration: ${sessionDuration / MILLIS_MULTIPLIER}.")
                if (sessionDuration > 0) {
                    val sessionId = UUID.randomUUID().toString()
                    MMPEventsManager.sendUserSessionEvent(sessionId, sessionStartTime, sessionDuration)
                }
            } catch (e: Exception) {
                logError("Error saving previous session", e)
            }
        } else {
            logDebug("No previous session found.")
        }
    }

    private fun initializeNewSession() {
        logDebug("Initializing new user session.")
        val now = System.currentTimeMillis()
        userSharedPreferences.saveSessionStartTime(now)
        userSharedPreferences.saveSessionEndTime(now)
    }

    private fun createNewUpdater() {
        updater = scheduler.scheduleWithFixedDelay({
            try {
                userSharedPreferences.saveSessionEndTime(System.currentTimeMillis())
            } catch (e: Exception) {
                logError("Error updating session_end", e)
            }
        }, SESSION_DURATION_SECONDS, SESSION_DURATION_SECONDS, TimeUnit.SECONDS)
    }
}
