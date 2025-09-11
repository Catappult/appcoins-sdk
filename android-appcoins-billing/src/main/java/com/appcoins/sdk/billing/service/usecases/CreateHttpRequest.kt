package com.appcoins.sdk.billing.service.usecases

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.BdsHttpService
import com.appcoins.sdk.billing.service.IpAddressCacheManager
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.service.helpers.UrlBuilderHelper
import com.appcoins.sdk.billing.usecases.UseCase
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils.sdkAnalytics
import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLPeerUnverifiedException

object CreateHttpRequest : UseCase() {

    @Suppress("ReturnCount", "LongMethod")
    operator fun invoke(
        baseUrl: String,
        endPoint: String?,
        httpMethod: String,
        paths: List<String>?,
        queries: Map<String, String>,
        header: Map<String, String>?,
        body: Map<String, Any>?,
        sdkBackendRequestType: SdkBackendRequestType,
        timeoutInMillis: Int,
    ): RequestResponse {
        var retryCount = 0
        val originalHost: String
        val httpService = BdsHttpService()
        val ipAddressCacheManager = IpAddressCacheManager()
        val urlBuilderHelper = UrlBuilderHelper(ipAddressCacheManager)

        try {
            originalHost = URL(baseUrl).host
        } catch (exception: MalformedURLException) {
            logError("Malformed base URL provided: $baseUrl. Error: $exception", exception)
            return GetRequestResponseFromException(null, exception)
        }

        while (retryCount <= MAX_DNS_RETRIES) {
            var urlConnection: HttpURLConnection? = null
            var urlToConnect: String? = null
            try {
                urlToConnect = urlBuilderHelper.buildUrlForAttempt(
                    baseUrl, endPoint, paths, queries, originalHost, retryCount
                ).also { logDebug("Url -> $it (Attempt: ${retryCount + 1})") }

                val url = URL(urlToConnect)
                val isIpAddressConnection = retryCount > 0 && ipAddressCacheManager.get(originalHost) != null

                val originalHostForVerification = if (isIpAddressConnection) originalHost else null

                sdkAnalytics.sendBackendRequestEvent(
                    sdkBackendRequestType,
                    urlToConnect,
                    httpMethod,
                    paths,
                    header,
                    queries,
                    body
                )

                urlConnection = httpService.openUrlConnection(url, httpMethod, originalHostForVerification)
                httpService.configureConnection(
                    urlConnection,
                    originalHost,
                    isIpAddressConnection,
                    header,
                    httpMethod,
                    body,
                    timeoutInMillis
                )

                val responseCode = urlConnection.responseCode
                val inputStream = httpService.getInputStream(urlConnection)
                val requestResponse = GetRequestResponseFromInputStream(inputStream, responseCode)
                val errorMessage = requestResponse.exception?.message

                sdkAnalytics.sendBackendResponseEvent(
                    sdkBackendRequestType,
                    responseCode,
                    requestResponse.response,
                    errorMessage
                )
                if (isIpAddressConnection) {
                    sdkAnalytics.sendBackendDnsManualCacheSuccessEvent(
                        urlToConnect,
                        ipAddressCacheManager.get(originalHost) ?: "",
                        responseCode
                    )
                }

                ipAddressCacheManager.cacheIp(originalHost)
                return requestResponse
            } catch (exception: UnknownHostException) {
                handleDnsFailure(
                    originalHost,
                    retryCount,
                    sdkBackendRequestType,
                    exception
                )
                if (retryCount < MAX_DNS_RETRIES) {
                    retryCount++
                    continue
                }
                logError("Max DNS retries reached for $originalHost. Giving up.")
                return GetRequestResponseFromException(null, exception)
            } catch (exception: Exception) {
                logAndHandleException(exception, urlToConnect, sdkBackendRequestType)
                return GetRequestResponseFromException(null, exception)
            } finally {
                urlConnection?.disconnect()
            }
        }
        return RequestResponse(HTTP_EXCEPTION_REQUEST_CODE, null, IOException("Unknown error after retries."))
    }

    private fun handleDnsFailure(
        originalHost: String,
        retryCount: Int,
        sdkBackendRequestType: SdkBackendRequestType,
        exception: UnknownHostException,
    ) {
        logError(
            "DNS resolution failed for $originalHost. " +
                "Attempt: ${retryCount + 1}/${MAX_DNS_RETRIES + 1}. " +
                "Error: ${exception.message}",
            exception
        )
        sdkAnalytics.sendBackendErrorEvent(
            sdkBackendRequestType,
            originalHost,
            "${exception.message}: $exception",
            WalletUtils.context
        )
        if (retryCount < MAX_DNS_RETRIES) {
            try {
                TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS)
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                logError("Retry delay interrupted: ${exception.message}")
                throw exception
            }
        }
    }

    private fun logAndHandleException(
        exception: Exception,
        urlToConnect: String?,
        sdkBackendRequestType: SdkBackendRequestType
    ) {
        val errorMessage = when (exception) {
            is MalformedURLException, is URISyntaxException -> "URL/URI construction error"
            is SSLPeerUnverifiedException -> "SSL Peer Verification failed"
            else -> "Request failed"
        }
        logError("$errorMessage for $urlToConnect. Error: ${exception.message}", exception)
        sdkAnalytics.sendBackendErrorEvent(
            sdkBackendRequestType,
            urlToConnect ?: "Unknown URL",
            "$errorMessage: $exception",
            WalletUtils.context
        )
    }

    private const val HTTP_EXCEPTION_REQUEST_CODE = 500
    private const val MAX_DNS_RETRIES = 2
    private const val RETRY_DELAY_MS = 1000L
}
