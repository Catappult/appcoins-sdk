package com.appcoins.sdk.ingameupdates.services

/**
 * @param responseCode Response code returned by the request, 500 by default (if there's a non IO
 * exception))
 * @param response Response returned by the request
 * @param exception Exception returned by the request
 */
class RequestResponse (val responseCode: Int, val response: String?, val exception: Exception?)
