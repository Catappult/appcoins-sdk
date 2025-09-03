package com.appcoins.sdk.billing.service

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.utils.RequestBuilderUtils
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class BdsHttpService {

    @Throws(IOException::class)
    fun openUrlConnection(
        url: URL,
        httpMethod: String,
        originalHostForVerification: String? = null
    ): HttpURLConnection {
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.requestMethod = httpMethod

        if (urlConnection is HttpsURLConnection && originalHostForVerification != null) {
            urlConnection.hostnameVerifier = BdsHostnameVerifier(originalHostForVerification)
        }
        return urlConnection
    }

    fun configureConnection(
        urlConnection: HttpURLConnection,
        originalHost: String?,
        isIpAddressConnection: Boolean,
        header: Map<String, String>?,
        httpMethod: String,
        body: Map<String, Any>?,
        timeoutInMillis: Int
    ) {
        setOriginalHost(urlConnection, originalHost, isIpAddressConnection)
        urlConnection.readTimeout = timeoutInMillis
        setUserAgent(urlConnection)
        setHeaders(urlConnection, header)
        handlePostPatchRequests(urlConnection, httpMethod, body)
    }

    @Throws(IOException::class)
    fun getInputStream(urlConnection: HttpURLConnection): InputStream =
        if (urlConnection.responseCode >= HTTP_STATUS_BAD_REQUEST) {
            urlConnection.errorStream
        } else {
            urlConnection.inputStream
        }

    // Private helper methods for connection configuration
    private fun setOriginalHost(
        urlConnection: HttpURLConnection,
        originalHost: String?,
        isIpAddressConnection: Boolean
    ) {
        if (isIpAddressConnection) {
            urlConnection.setRequestProperty("Host", originalHost)
        }
    }

    private fun setUserAgent(urlConnection: HttpURLConnection) {
        urlConnection.setRequestProperty("User-Agent", WalletUtils.userAgent)
    }

    private fun setHeaders(urlConnection: HttpURLConnection, header: Map<String, String>?) {
        header?.forEach { (key, value) ->
            urlConnection.setRequestProperty(key, value)
        }
    }

    @Throws(IOException::class)
    private fun handlePostPatchRequests(urlConnection: HttpURLConnection, httpMethod: String, body: Map<String, Any>?) {
        if (isValidPostPatchRequest(httpMethod, body)) {
            if (httpMethod == "PATCH") {
                urlConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH")
            }
            setPostOutput(urlConnection, body)
        }
    }

    private fun isValidPostPatchRequest(httpMethod: String, body: Map<String, Any>?): Boolean {
        return (httpMethod == "POST" || httpMethod == "PATCH") && body != null
    }

    @Throws(IOException::class)
    private fun setPostOutput(urlConnection: HttpURLConnection, bodyKeys: Map<String, Any>?) {
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.setRequestProperty("Accept", "application/json")
        urlConnection.doOutput = true
        val os = urlConnection.outputStream
        val body = RequestBuilderUtils.buildBody(bodyKeys)
        val input = body.toByteArray()
        os.write(input, 0, input.size)
    }

    companion object {
        private const val HTTP_STATUS_BAD_REQUEST = 400
    }
}
