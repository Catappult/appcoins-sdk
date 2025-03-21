package com.appcoins.sdk.core.device

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager
import com.appcoins.sdk.core.logger.Logger.logInfo


object QGenerator {
    private val supportedOpenGLExtensions = arrayOf(
        "GL_OES_compressed_ETC1_RGB8_texture", "GL_OES_compressed_paletted_texture",
        "GL_AMD_compressed_3DC_texture", "GL_AMD_compressed_ATC_texture",
        "GL_EXT_texture_compression_latc", "GL_EXT_texture_compression_dxt1",
        "GL_EXT_texture_compression_s3tc", "GL_ATI_texture_compression_atitc",
        "GL_IMG_texture_compression_pvrtc"
    )

    fun generateQ(context: Context): String {
        val minSdk = Build.VERSION.SDK_INT
        val minScreen = Size.values()[getScreenSizeInt(context)].name.lowercase()
        val minGlEs =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo.glEsVersion
        val density = getDensityDpi(context)
        val cpuAbi = abis
        var filters = ((if (Build.DEVICE == "alien_jolla_bionic") "apkdwn=myapp&" else "")
            + "maxSdk=" + minSdk + "&maxScreen=" + minScreen + "&maxGles=" + minGlEs
            + "&myCPU=" + cpuAbi + "&myDensity=" + density)
        filters = addOpenGLExtensions(filters)
        return Base64.encodeToString(filters.toByteArray(), 0).replace("=", "").replace("/", "*")
            .replace("+", "_").replace("\n", "")
    }

    private fun addOpenGLExtensions(filters: String): String {
        val openGLExtensions =
            OpenGLHelper().getDeviceSupportedExtensions().apply { logInfo(this.toString()) }
        var updatedFilters = filters
        var extensionAdded = false
        openGLExtensions?.forEach { extension ->
            if (listOf(*supportedOpenGLExtensions).contains(extension)) {
                updatedFilters +=
                    if (!extensionAdded) {
                        "&myGLTex=$extension"
                    } else {
                        ",$extension"
                    }
                extensionAdded = true
            }
        }
        return updatedFilters
    }

    private fun getScreenSizeInt(context: Context): Int {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
    }

    private val abis: String
        get() {
            val abis =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Build.SUPPORTED_ABIS
                } else {
                    arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
                }
            val builder = StringBuilder()
            for (i in abis.indices) {
                builder.append(abis[i])
                if (i < abis.size - 1) {
                    builder.append(",")
                }
            }
            return builder.toString()
        }

    private fun getDensityDpi(context: Context): Int {
        val metrics = DisplayMetrics()
        (context.getSystemService(Service.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)
        val dpi = metrics.densityDpi
        return when {
            dpi <= 120 -> 120
            dpi <= 160 -> 160
            dpi <= 213 -> 213
            dpi <= 240 -> 240
            dpi <= 320 -> 320
            dpi <= 480 -> 480
            else -> 640
        }
    }

    private enum class Size {
        NOTFOUND, SMALL, NORMAL, LARGE, XLARGE
    }
}
