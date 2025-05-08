package com.appcoins.sdk.billing

enum class FeatureType(val value: Int) {

    /**
     * Feature of Subscriptions.
     */
    SUBSCRIPTIONS(0),

    /**
     * Availability of obfuscatedAccountId parameter for launching Purchase Flow for current Billing Service.
     */
    OBFUSCATED_ACCOUNT_ID(1),

    /**
     * Feature of Free Trials.
     */
    FREE_TRIALS(2),
}
