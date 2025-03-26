package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class NewVersionAvailableResponseMapper {
    fun map(response: RequestResponse): NewVersionAvailableResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError(
                "Failed to obtain New Version Available Response. " +
                    "ResponseCode: ${response.responseCode} | Cause: ${response.exception}"
            )
            return NewVersionAvailableResponse(response.responseCode)
        }

        return try {
            val responseJsonObject = JSONObject(response.response)

            val isNewVersionAvailable = responseJsonObject.optBoolean("is_new_version_available", false)
            val failureMessage = responseJsonObject.optString("failure_message").takeIf { it.isNotEmpty() }

            if (failureMessage != null) {
                throw UnsupportedOperationException(failureMessage)
            }

            NewVersionAvailableResponse(response.responseCode, isNewVersionAvailable)
        } catch (ex: Exception) {
            logError("There was an error mapping the response.", ex)
            SdkAnalyticsUtils.sdkAnalytics.sendBackendMappingFailureEvent(
                SdkBackendRequestType.NEW_VERSION_AVAILABLE,
                response.response,
                ex.toString()
            )
            NewVersionAvailableResponse(response.responseCode)
        }
    }
}

data class NewVersionAvailableResponse(
    val responseCode: Int?,
    val isNewVersionAvailable: Boolean = false
)
