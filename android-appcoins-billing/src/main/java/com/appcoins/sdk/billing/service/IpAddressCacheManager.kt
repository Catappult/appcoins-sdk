package com.appcoins.sdk.billing.service

import com.appcoins.sdk.core.logger.Logger.logError
import java.net.InetAddress
import java.net.UnknownHostException

class IpAddressCacheManager {

    fun get(host: String): String? {
        return IpAddressCache.get(host)
    }

    fun cacheIp(originalHost: String?) {
        try {
            if (originalHost != null) {
                val resolvedIp = InetAddress.getByName(originalHost).hostAddress
                IpAddressCache.put(originalHost, resolvedIp)
            }
        } catch (ipEx: UnknownHostException) {
            logError(
                "Could not resolve IP for caching after successful request for $originalHost: ${ipEx.message}",
                ipEx
            )
        }
    }
}
