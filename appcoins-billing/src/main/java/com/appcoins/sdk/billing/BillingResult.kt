package com.appcoins.sdk.billing

class BillingResult internal constructor(val responseCode: Int? = null, val debugMessage: String? = "") {

    companion object {
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder internal constructor() {
        private var responseCode: Int? = null
        private var debugMessage: String = ""

        fun setResponseCode(responseCode: Int): Builder {
            this.responseCode = responseCode
            return this
        }

        fun setDebugMessage(debugMessage: String): Builder {
            this.debugMessage = debugMessage
            return this
        }

        fun build(): BillingResult {
            return BillingResult(responseCode, debugMessage)
        }
    }

    override fun toString(): String {
        return "ResponseCode: $responseCode, DebugMessage: $debugMessage"
    }
}
