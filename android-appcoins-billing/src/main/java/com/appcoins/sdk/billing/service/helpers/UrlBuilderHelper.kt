package com.appcoins.sdk.billing.service.helpers

import com.appcoins.sdk.billing.service.IpAddressCacheManager
import com.appcoins.sdk.billing.utils.RequestBuilderUtils
import java.net.URI

class UrlBuilderHelper(private val ipAddressCacheManager: IpAddressCacheManager) {

    fun buildUrlForAttempt(
        baseUrl: String,
        endPoint: String?,
        paths: List<String>?,
        queries: Map<String, String>,
        originalHost: String,
        retryCount: Int
    ): String {
        if (retryCount == 0) {
            return RequestBuilderUtils.buildUrl(baseUrl, endPoint, paths, queries)
        }

        val cachedIp = ipAddressCacheManager.get(originalHost)
        if (cachedIp != null) {
            val uri = URI(RequestBuilderUtils.buildUrl(baseUrl, endPoint, paths, queries))
            val scheme = uri.scheme
            val pathAndQuery = createPathAndQuery(uri)
            return "$scheme://$cachedIp$pathAndQuery"
        }

        return RequestBuilderUtils.buildUrl(baseUrl, endPoint, paths, queries)
    }

    private fun createPathAndQuery(uri: URI): String {
        var path = uri.rawPath
        if (uri.rawQuery != null) {
            path += "?${uri.rawQuery}"
        }
        return path
    }
}
