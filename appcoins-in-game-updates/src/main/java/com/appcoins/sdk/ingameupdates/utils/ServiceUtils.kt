package com.appcoins.sdk.ingameupdates.utils

object ServiceUtils {
    fun isSuccess(code: Int): Boolean =
        code in 200..299
}
