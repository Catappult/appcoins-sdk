package com.appcoins.sdk.billing.sharedpreferences

import android.content.Context
import com.appcoins.sdk.billing.models.WalletDetails

@Suppress("complexity:TooManyFunctions")
class AptoideWalletSharedPreferences(context: Context) : SharedPreferencesRepository(context) {

    fun setWalletDetails(walletDetails: WalletDetails) {
        if (walletDetails.hasError()) {
            setWalletAddress(null)
            setWalletToken(null)
            setExpirationTimeMillis(0)
            return
        }
        setWalletAddress(walletDetails.walletAddress)
        setWalletToken(walletDetails.walletToken)
        setExpirationTimeMillis(walletDetails.expirationTimeMillis)
    }

    fun getWalletDetails(): WalletDetails {
        val walletAddress = getWalletAddress()
        val walletToken = getWalletToken()
        val expirationTimeMillis = getExpirationTimeMillis()
        if (walletAddress == null || walletToken == null) {
            return WalletDetails.createErrorWalletDetails()
        }
        return WalletDetails(walletAddress, walletToken, expirationTimeMillis, false)
    }

    fun setWalletAddress(value: String?) = setString(WALLET_ADDRESS_KEY, value)

    fun getWalletAddress(): String? = getString(WALLET_ADDRESS_KEY)

    fun setWalletToken(value: String?) = setString(WALLET_TOKEN_KEY, value)

    fun getWalletToken(): String? = getString(WALLET_TOKEN_KEY)

    fun setExpirationTimeMillis(value: Long) = setLong(EXPIRATION_TIME_MILLIS_KEY, value)

    fun getExpirationTimeMillis() = getLong(EXPIRATION_TIME_MILLIS_KEY)

    private companion object {
        const val WALLET_ADDRESS_KEY = "WALLET_ADDRESS"
        const val WALLET_TOKEN_KEY = "WALLET_TOKEN"
        const val EXPIRATION_TIME_MILLIS_KEY = "EXPIRATION_TIME_MILLIS"
    }
}
