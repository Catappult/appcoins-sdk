package com.appcoins.sdk.core.device

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager

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
        var filters =
            (
                (if (Build.DEVICE == "alien_jolla_bionic") "apkdwn=myapp&" else "") +
                    "maxSdk=" + minSdk + "&maxScreen=" + minScreen +
                    "&maxGles=" + minGlEs + "&myCPU=" + cpuAbi + "&myDensity=" + density
                )
        filters = addOpenGLExtensions(filters)
        return Base64.encodeToString(filters.toByteArray(), 0).replace("=", "").replace("/", "*")
            .replace("+", "_").replace("\n", "")
    }

    private fun addOpenGLExtensions(filters: String): String {
        val openGLExtensions = OpenGLHelper().getDeviceSupportedExtensions()
        var updatedFilters = filters
        var extensionAdded = false
        openGLExtensions?.forEach { extension ->
            if (supportedOpenGLExtensions.contains(extension)) {
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
            dpi <= DPI_120 -> DPI_120
            dpi <= DPI_160 -> DPI_160
            dpi <= DPI_213 -> DPI_213
            dpi <= DPI_240 -> DPI_240
            dpi <= DPI_320 -> DPI_320
            dpi <= DPI_480 -> DPI_480
            else -> DPI_640
        }
    }

    private enum class Size {
        NOTFOUND, SMALL, NORMAL, LARGE, XLARGE
    }

    private const val DPI_120 = 120
    private const val DPI_160 = 160
    private const val DPI_213 = 213
    private const val DPI_240 = 240
    private const val DPI_320 = 320
    private const val DPI_480 = 480
    private const val DPI_640 = 640
}
