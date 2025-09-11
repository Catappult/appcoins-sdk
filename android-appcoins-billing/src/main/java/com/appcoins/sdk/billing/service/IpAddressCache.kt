package com.appcoins.sdk.billing.service

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.sharedpreferences.BackendRequestsSharedPreferences
import com.appcoins.sdk.core.logger.Logger.logDebug
import java.util.concurrent.ConcurrentHashMap

/**
 * Internal class for caching IP addresses.
 */
object IpAddressCache {
    private val backendRequestsSharedPreferences: BackendRequestsSharedPreferences by lazy {
        BackendRequestsSharedPreferences(WalletUtils.context)
    }
    private val concurrentHashMap: ConcurrentHashMap<String, String> by lazy {
        backendRequestsSharedPreferences.getIpAddressesCache()
    }

    /**
     * Stores or updates an IP address for a given domain.
     * @param domain The domain name.
     * @param ipAddress The IP address to cache.
     */
    fun put(domain: String?, ipAddress: String?) {
        if (!domain.isNullOrEmpty()) {
            backendRequestsSharedPreferences.setIpAddressesCache(domain, ipAddress)
            if (ipAddress != null) {
                concurrentHashMap[domain] = ipAddress
            } else {
                concurrentHashMap.remove(domain)
            }
            logDebug("Cached IP $ipAddress for domain $domain")
        }
    }

    /**
     * Retrieves a cached IP address for a given domain.
     * @param domain The domain name to look up.
     * @return The cached IP address, or null if not found.
     */
    fun get(domain: String?): String? {
        if (!domain.isNullOrEmpty()) {
            val ip = concurrentHashMap[domain]
            if (ip != null) {
                logDebug("Retrieved cached IP $ip for domain $domain")
            }
            return ip
        }
        return null
    }
}
