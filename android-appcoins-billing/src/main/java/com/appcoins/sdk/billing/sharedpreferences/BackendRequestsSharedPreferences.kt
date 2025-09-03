package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context
import android.util.Base64
import com.appcoins.sdk.billing.service.RequestData
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.ConcurrentHashMap

class BackendRequestsSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun getIpAddressesCache(): ConcurrentHashMap<String, String> {
        val concurrentHashMap = ConcurrentHashMap<String, String>()
        val jsonArrayString = getString(IP_ADDRESSES_CACHE_KEY)
        return if (!jsonArrayString.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(jsonArrayString)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val domain = jsonObject.getString("domain")
                    val ip = jsonObject.getString("ip")
                    concurrentHashMap[domain] = ip
                }
            } catch (e: Exception) {
                logError("There was an error getting the IP Addresses Cache.", e)
            }
            concurrentHashMap
        } else {
            concurrentHashMap
        }
    }

    fun setIpAddressesCache(domain: String, ip: String?) {
        val concurrentHashMap = getIpAddressesCache()
        if (ip == null) {
            concurrentHashMap.remove(domain)
        } else {
            concurrentHashMap[domain] = ip
        }
        val jsonArray = JSONArray()
        for ((key, value) in concurrentHashMap) {
            val jsonObject = JSONObject()
            jsonObject.put("domain", key)
            jsonObject.put("ip", value)
            jsonArray.put(jsonObject)
        }
        setString(IP_ADDRESSES_CACHE_KEY, jsonArray.toString())
    }

    fun getFailedRequests(): List<RequestData>? {
        val serializedList = getString(FAILED_REQUESTS_KEY)
        return if (serializedList != null) {
            try {
                val bytes = Base64.decode(serializedList, Base64.DEFAULT)
                ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as? List<RequestData> }
            } catch (e: Exception) {
                logError("There was an error getting the Failed Requests.", e)
                null
            }
        } else {
            null
        }
    }

    fun setFailedRequests(value: List<RequestData>? = null) {
        if (value.isNullOrEmpty()) {
            setString(FAILED_REQUESTS_KEY, null)
            return
        }
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            ObjectOutputStream(byteArrayOutputStream).use { it.writeObject(value) }
            val serializedList = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
            setString(FAILED_REQUESTS_KEY, serializedList)
        } catch (e: Exception) {
            logError("There was an error setting the Failed Requests.", e)
        }
    }

    private companion object {
        const val FAILED_REQUESTS_KEY = "FAILED_REQUESTS"
        const val IP_ADDRESSES_CACHE_KEY = "IP_ADDRESSES_CACHE"
    }
}
