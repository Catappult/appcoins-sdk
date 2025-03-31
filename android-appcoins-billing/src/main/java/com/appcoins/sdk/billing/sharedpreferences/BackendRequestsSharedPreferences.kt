package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context
import android.util.Base64
import com.appcoins.sdk.billing.service.RequestData
import com.appcoins.sdk.core.logger.Logger.logError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class BackendRequestsSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

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
    }
}
