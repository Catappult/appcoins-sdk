package com.appcoins.sdk.billing

class QueryPurchasesParams private constructor(val productType: String) {
    class Builder {

        private var productType: String? = null

        fun setProductType(productType: String): Builder {
            require(productType.isNotEmpty()) { "Product type must be provided." }

            this.productType = productType
            return this
        }

        fun build(): QueryPurchasesParams {
            requireNotNull(this.productType) { "Product type must be provided." }

            return QueryPurchasesParams(productType!!)
        }
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }
    }
}
