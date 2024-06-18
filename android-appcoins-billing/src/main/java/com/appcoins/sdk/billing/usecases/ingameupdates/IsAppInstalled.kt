package com.appcoins.sdk.billing.usecases.ingameupdates

import android.content.Context
import android.content.pm.PackageManager

object IsAppInstalled {

    fun invoke(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
