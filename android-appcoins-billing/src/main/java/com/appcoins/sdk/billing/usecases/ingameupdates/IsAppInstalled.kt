package com.appcoins.sdk.billing.usecases.ingameupdates

import android.content.Context
import android.content.pm.PackageManager
import com.appcoins.sdk.billing.usecases.UseCase

object IsAppInstalled : UseCase() {

    operator fun invoke(context: Context, packageName: String): Boolean {
        super.invokeUseCase()
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
