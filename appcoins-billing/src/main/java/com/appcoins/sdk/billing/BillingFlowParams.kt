package com.appcoins.sdk.billing

class BillingFlowParams
@Deprecated("Deprecated constructor. Use the [Builder] to create the BillingFlowParams.")
constructor(
    val sku: String,
    val skuType: String,
    val orderReference: String?,
    val developerPayload: String?,
    val origin: String?,
) {
    var obfuscatedAccountId: String? = null
    var freeTrial: Boolean? = null

    @Deprecated("Deprecated constructor. Use the [Builder] to create the BillingFlowParams.")
    constructor(
        sku: String,
        skuType: String,
        orderReference: String?,
        developerPayload: String?,
        origin: String?,
        obfuscatedAccountId: String?,
        freeTrial: Boolean?
    ) : this(sku, skuType, orderReference, developerPayload, origin) {
        this.obfuscatedAccountId = obfuscatedAccountId
        this.freeTrial = freeTrial
    }

    class Builder {
        private var productDetailsParamsList: List<ProductDetailsParams> = emptyList()
        private var developerPayload: String? = null
        private var obfuscatedAccountId: String? = null
        private var freeTrial: Boolean? = null

        fun setProductDetailsParamsList(productDetailsParamsList: List<ProductDetailsParams>): Builder {
            require(productDetailsParamsList.isNotEmpty()) { "Product list must not be empty." }
            require(productDetailsParamsList.size == 1) { "Only one product is supported." }

            this.productDetailsParamsList = productDetailsParamsList
            return this
        }

        @Deprecated(
            "Deprecated parameter. " +
                "DeveloperPayload should not be used to identify Purchases. " +
                "Use instead the purchaseToken. " +
                "If needed to identify the User use the [obfuscatedAccountId] parameter."
        )
        fun setDeveloperPayload(developerPayload: String): Builder {
            require(developerPayload.isNotEmpty()) { "Developer Payload must not be empty. Use null if not necessary" }
            this.developerPayload = developerPayload
            return this
        }

        fun setObfuscatedAccountId(obfuscatedAccountId: String): Builder {
            require(obfuscatedAccountId.isNotEmpty()) {
                "Obfuscated Account ID must not be empty. Use null if not necessary"
            }
            this.obfuscatedAccountId = obfuscatedAccountId
            return this
        }

        fun setFreeTrial(freeTrial: Boolean): Builder {
            this.freeTrial = freeTrial
            return this
        }

        fun build(): BillingFlowParams {
            requireNotNull(this.productDetailsParamsList) { "ProductDetailsParams list must be provided." }
            require(this.productDetailsParamsList.isNotEmpty()) { "ProductDetailsParams list must not be empty." }

            val productDetails = this.productDetailsParamsList.first().productDetails

            return BillingFlowParams(
                productDetails.productId,
                productDetails.productType,
                null,
                developerPayload,
                null,
                obfuscatedAccountId,
                freeTrial
            )
        }
    }

    class ProductDetailsParams private constructor(val productDetails: ProductDetails) {

        class Builder {
            private var productDetails: ProductDetails? = null

            fun setProductDetails(productDetails: ProductDetails): Builder {
                this.productDetails = productDetails
                return this
            }

            fun build(): ProductDetailsParams {
                requireNotNull(this.productDetails) {
                    "ProductDetails is required for constructing ProductDetailsParams."
                }

                return ProductDetailsParams(productDetails!!)
            }
        }

        companion object {
            @JvmStatic
            fun newBuilder(): Builder {
                return Builder()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }
}
