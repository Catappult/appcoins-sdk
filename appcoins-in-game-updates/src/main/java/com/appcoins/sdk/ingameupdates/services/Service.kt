package com.appcoins.sdk.ingameupdates.services

interface Service {
    /**
     * @param endPoint String to be added to the base url of the request
     * @param httpMethod Method of the request to be made: GET, POST, PATCH, DELETE
     * @param paths List of paths to be added to the url
     * @param queries Map of the key values to be added as query
     * @param header Map of the key values to be added to the header
     * @param body Map of the key values to be added to the body.
     * @param serviceResponseListener Listener in which the response will be sent.
     */
    fun makeRequest(
        endPoint: String?, httpMethod: String?, paths: List<String?>?,
        queries: Map<String?, String?>?, header: Map<String?, String?>?, body: Map<String?, Any?>?,
        serviceResponseListener: ServiceResponseListener?
    )

    /**
     * Cancel all async tasks currently in execution
     * This method should call when the view is destroyed
     */
    fun cancelRequests()
}
