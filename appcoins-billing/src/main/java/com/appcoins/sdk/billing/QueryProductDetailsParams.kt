package com.appcoins.sdk.billing

class QueryProductDetailsParams private constructor(val productList: List<Product>) {

    companion object {
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {

        private var productList: List<Product> = emptyList()

        fun setProductList(productList: List<Product>): Builder {
            require(productList.isNotEmpty()) { "Product list must not be empty." }
            require(productList.distinctBy { it.productType }.size == 1) { "All products must be of the same type." }
            require(
                productList.distinctBy { it.productId }.size == productList.size
            ) { "Product id should not be repeated." }

            this.productList = productList
            return this
        }

        fun build(): QueryProductDetailsParams {
            require(this.productList.isNotEmpty()) { "Product list must not be empty." }

            return QueryProductDetailsParams(productList)
        }
    }

    class Product private constructor(val productId: String, val productType: String) {

        companion object {
            @JvmStatic
            fun newBuilder(): Builder {
                return Builder()
            }
        }

        class Builder {
            private var productId: String? = null
            private var productType: String? = null

            fun setProductId(productId: String): Builder {
                this.productId = productId
                return this
            }

            fun setProductType(productType: String): Builder {
                this.productType = productType
                return this
            }

            fun build(): Product {
                requireNotNull(this.productId) { "Product id must be provided." }
                requireNotNull(this.productType) { "Product type must be provided." }

                return Product(productId!!, productType!!)
            }
        }
    }
}
