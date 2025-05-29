package com.appcoins.sdk.billing

class ConsumeParams private constructor(val purchaseToken: String) {
    class Builder {

        private var purchaseToken: String? = null

        fun setPurchaseToken(purchaseToken: String): Builder {
            require(purchaseToken.isNotEmpty()) { "Purchase token must not be empty." }

            this.purchaseToken = purchaseToken
            return this
        }

        fun build(): ConsumeParams {
            requireNotNull(this.purchaseToken) { "Purchase token must be provided." }

            return ConsumeParams(purchaseToken!!)
        }
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }
    }
}
