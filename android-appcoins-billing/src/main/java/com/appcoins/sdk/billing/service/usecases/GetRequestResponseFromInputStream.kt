package com.appcoins.sdk.billing.service.usecases

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.usecases.UseCase
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object GetRequestResponseFromInputStream : UseCase() {
    operator fun invoke(inputStream: InputStream, responseCode: Int): RequestResponse {
        val inputStreamReader = BufferedReader(InputStreamReader(inputStream))
        var inputLine: String?
        val response = StringBuilder()

        while ((inputStreamReader.readLine().also { inputLine = it }) != null) {
            response.append(inputLine)
        }
        inputStreamReader.close()
        return RequestResponse(responseCode, response.toString(), null)
    }
}
