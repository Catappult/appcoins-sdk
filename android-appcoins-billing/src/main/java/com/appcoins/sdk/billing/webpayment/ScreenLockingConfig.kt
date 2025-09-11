package com.appcoins.sdk.billing.webpayment

internal class ScreenLockingConfig private constructor(
    val lockCloseView: Boolean,
    val lockBackButton: Boolean,
    val timeout: Int,
) {

    class Builder {
        private var lockCloseView: Boolean = false
        private var lockBackButton: Boolean = false
        private var timeout: Int = TIMEOUT_DEFAULT

        fun setLockCloseView(value: Boolean) = apply { lockCloseView = value }
        fun setLockBackButton(value: Boolean) = apply { lockBackButton = value }
        fun setTimeout(value: Int) = apply { timeout = value }

        fun build(): ScreenLockingConfig {
            return ScreenLockingConfig(lockCloseView, lockBackButton, timeout)
        }
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }

        const val TIMEOUT_DEFAULT = 30
    }
}
