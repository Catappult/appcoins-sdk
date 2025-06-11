package com.appcoins.sdk.billing

/**
 * Class with the response code and error message if present.
 *
 * @param responseCode BillingResponseCode with the integer value for the response.
 * @param debugMessage Error message if present for debugging purposes.
 */
class BillingResult internal constructor(val responseCode: Int, val debugMessage: String?) {

    companion object {
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder internal constructor() {
        private var responseCode: Int? = null
        private var debugMessage: String? = null

        fun setResponseCode(responseCode: Int): Builder {
            this.responseCode = responseCode
            return this
        }

        fun setDebugMessage(debugMessage: String?): Builder {
            this.debugMessage = debugMessage
            return this
        }

        fun build(): BillingResult {
            return BillingResult(responseCode ?: 6, debugMessage)
        }
    }

    override fun toString(): String {
        return "ResponseCode: $responseCode, DebugMessage: $debugMessage"
    }
}
