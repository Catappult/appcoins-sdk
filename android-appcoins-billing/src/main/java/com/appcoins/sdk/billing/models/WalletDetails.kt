package com.appcoins.sdk.billing.models

data class WalletDetails(
    val walletAddress: String,
    val walletToken: String,
    val expirationTimeMillis: Long,
    private val error: Boolean
) {

    fun hasError(): Boolean {
        return error
    }

    private constructor() : this(
        walletAddress = "",
        walletToken = "",
        expirationTimeMillis = 0,
        error = true
    )

    companion object {
        fun createErrorWalletDetails(): WalletDetails {
            return WalletDetails()
        }
    }
}
