package com.appcoins.sdk.billing.service

import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType
import java.io.Serializable

/**
 * @param baseUrl Base URL to which the request will be performed.
 * @param timeoutInMillis Timeout for the Request.
 * @param endPoint Endpoint after the Base URL to which the request will be performed.
 * @param httpMethod HTTP Method to be used in the request.
 * @param paths List of the Paths to add to the request.
 * @param queries List of the Queries to add to the request.
 * @param header List of the Headers to add to the request.
 * @param body Body to add to the request.
 */
class RequestData(
    val sdkBackendRequestType: SdkBackendRequestType,
    val baseUrl: String,
    val timeoutInMillis: Int,
    val endPoint: String?,
    val httpMethod: String,
    val paths: List<String>,
    val queries: Map<String, String>,
    val header: Map<String, String>,
    val body: Map<String, Any>,
) : Serializable
