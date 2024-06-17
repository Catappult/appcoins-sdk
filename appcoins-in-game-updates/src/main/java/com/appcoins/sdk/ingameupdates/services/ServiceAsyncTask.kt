package com.appcoins.sdk.ingameupdates.services

import android.os.AsyncTask

class ServiceAsyncTask internal constructor(
    private val bdsService: BdsService,
    private val baseUrl: String,
    private val endPoint: String,
    private val httpMethod: String,
    private val paths: List<String>,
    private val queries: Map<String, String>,
    private val header: Map<String, String>,
    private val body: Map<String, Any>,
    private val serviceResponseListener: ServiceResponseListener?
) : AsyncTask<Any?, Any?, RequestResponse>() {
    override fun doInBackground(objects: Array<Any?>): RequestResponse {
        return bdsService.createRequest(baseUrl, endPoint, httpMethod, paths, queries, header, body)
    }

    override fun onPostExecute(requestResponse: RequestResponse) {
        super.onPostExecute(requestResponse)
        serviceResponseListener?.onResponseReceived(requestResponse)
    }
}
