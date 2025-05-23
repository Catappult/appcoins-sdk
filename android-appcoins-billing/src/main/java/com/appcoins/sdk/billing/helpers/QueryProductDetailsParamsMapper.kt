package com.appcoins.sdk.billing.helpers

import com.appcoins.sdk.billing.QueryProductDetailsParams

class QueryProductDetailsParamsMapper {
    fun mapProductDetailsListToProductIdsList(queryProductDetailsParams: QueryProductDetailsParams): List<String> =
        queryProductDetailsParams.productList.map { it.productId }

    fun getProductIdFromQueryProductDetailsParams(queryProductDetailsParams: QueryProductDetailsParams): String =
        queryProductDetailsParams.productList.first().productType
}
