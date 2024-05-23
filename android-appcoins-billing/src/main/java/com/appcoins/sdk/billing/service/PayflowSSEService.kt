package com.appcoins.sdk.billing.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.appcoins.sdk.billing.listeners.PayflowPriorityStream
import com.appcoins.sdk.billing.payflow.PayflowMethodResponse
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.billing.usecases.GetQueriesListForPayflowPriority
import com.appcoins.sdk.billing.utils.RequestBuilderUtils
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class PayflowSSEService : Service() {

    override fun onCreate() {
        super.onCreate()
        Thread { listenForSSE() }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun listenForSSE() {
        try {
            val urlBuilder = RequestBuilderUtils.buildUrl(
                SSE_URL,
                SSE_URL_ENDPOINT,
                emptyList(),
                GetQueriesListForPayflowPriority.invoke()
            )

            val url = URL(urlBuilder)
            val urlConnection = url.openConnection() as HttpURLConnection

            Log.d(TAG, "http response: " + urlConnection.responseCode)

            val inputStream: InputStream = BufferedInputStream(urlConnection.inputStream)
            readStream(inputStream)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            Log.e(TAG, "Error on url openConnection: " + e.message)
            e.printStackTrace()
            PayflowPriorityStream.getInstance().emit(null)
            /*PayflowPriorityStream.getInstance()
                .emit(
                    PayflowMethodResponse(
                        200,
                        listOf(PaymentFlowMethod.PayAsAGuest("pay_as_a_guest", 1))
                    )
                )*/
        }
    }

    private fun readStream(inputStream: InputStream) {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(inputStream))
            var line = ""
            while (true) {
                while (reader?.readLine()?.also { line = it } != null) {
                    Log.d(TAG, "SSE event: $line")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                    // TODO Create exponential retry mechanism
                    listenForSSE()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private companion object {
        private const val TAG = "SSEService"
        private const val SSE_URL = "https://my.testing.web"
        private const val SSE_URL_ENDPOINT = "/.sse"
    }
}
