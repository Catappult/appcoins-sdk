package com.appcoins.sdk.billing.service.usecases

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.logger.Logger.logError
import java.io.IOException
import java.net.HttpURLConnection

object GetRequestResponseFromException : UseCase() {
    operator fun invoke(urlConnection: HttpURLConnection?, firstException: Exception): RequestResponse {
        logError("Failed to create backend request: $firstException")
        var responseCode = HTTP_EXCEPTION_REQUEST_CODE
        if (urlConnection != null) {
            try {
                responseCode = urlConnection.responseCode
            } catch (ioException: IOException) {
                logError("Failed to read response code from request: $ioException")
            }
        }
        return RequestResponse(responseCode, null, firstException)
    }

    private const val HTTP_EXCEPTION_REQUEST_CODE = 500
}
