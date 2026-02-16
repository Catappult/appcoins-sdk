package com.appcoins.sdk.billing.managers

import com.appcoins.sdk.billing.payflow.models.featureflags.LimitSDKRequests
import com.appcoins.sdk.core.date.SECONDS_TO_MILLIS
import com.appcoins.sdk.core.logger.Logger.logWarning
import java.util.concurrent.ConcurrentHashMap

object LimitSDKRequestsManager {

    private val requestStates = ConcurrentHashMap<LimitSDKRequests.SDKRequestType, RequestState>()
    private val limits = ConcurrentHashMap<LimitSDKRequests.SDKRequestType, LimitSDKRequests.LimitDetails>()

    class RequestState {
        var count: Int = 0
        var windowStartTime: Long = 0L
        var blockedUntil: Long = 0L
    }

    fun updateRequestsLimits(limitSDKRequests: LimitSDKRequests?) = runCatching {
        requestStates.clear()
        limits.clear()
        limitSDKRequests?.limitDetailsList?.forEach { details ->
            details.sdkRequestTypes.forEach { type ->
                limits[type] = details
            }
        }
    }.getOrElse {
        logWarning("Error updating requests limits: ${it.message}")
    }

    fun canMakeRequest(type: LimitSDKRequests.SDKRequestType): Boolean = runCatching {
        val limit = limits[type] ?: return true
        val state = requestStates.getOrPut(type) { RequestState() }

        val currentTime = System.currentTimeMillis()

        if (state.blockedUntil > 0) {
            if (currentTime >= state.blockedUntil) {
                state.blockedUntil = 0L
                state.count = 0
                state.windowStartTime = 0L
                return true
            } else {
                return false
            }
        }

        val windowDurationMs = limit.rateLimitDuration * SECONDS_TO_MILLIS
        if (currentTime > state.windowStartTime + windowDurationMs) {
            state.blockedUntil = 0L
            state.count = 0
            state.windowStartTime = 0L
        }

        return state.count < limit.rateLimitCount
    }.getOrElse {
        logWarning("Error checking if request can be made: ${it.message}")
        true
    }

    fun onRequestMade(type: LimitSDKRequests.SDKRequestType) = runCatching {
        val limit = limits[type] ?: return@runCatching
        val state = requestStates.getOrPut(type) { RequestState() }

        val currentTime = System.currentTimeMillis()

        if (state.count == 0) {
            state.windowStartTime = currentTime
        }

        state.count++

        if (state.count >= limit.rateLimitCount && state.blockedUntil == 0L) {
            state.blockedUntil = currentTime + (limit.cooldownDuration * SECONDS_TO_MILLIS)
        }
    }.getOrElse {
        logWarning("Error handling request made: ${it.message}")
    }

    fun resetSDKRequestTypeCount(type: LimitSDKRequests.SDKRequestType) =
        runCatching {
            val state = requestStates[type] ?: return@runCatching
            state.count = 0
            state.blockedUntil = 0L
            state.windowStartTime = 0L
        }.getOrElse {
            logWarning("Error resetting request count: ${it.message}")
        }

    fun resetRequestsCount() {
        requestStates.clear()
    }
}
