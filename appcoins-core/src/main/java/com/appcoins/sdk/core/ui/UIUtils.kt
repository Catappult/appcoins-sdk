package com.appcoins.sdk.core.ui

import android.app.Activity.WINDOW_SERVICE
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import android.view.WindowManager

fun floatToPxs(value: Float, context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics)

fun getScreenHeightInDp(context: Context): Int =
    when (context.resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> getScreenOrientedWidthInDp(context)
        else -> getScreenOrientedHeightInDp(context)
    }

fun getScreenWidthInDp(context: Context): Int =
    when (context.resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> getScreenOrientedHeightInDp(context)
        else -> getScreenOrientedWidthInDp(context)
    }

fun getScreenOrientedHeightInDp(context: Context): Int =
    (context.resources.displayMetrics.heightPixels / context.resources.displayMetrics.density).toInt()

fun getScreenOrientedWidthInDp(context: Context): Int =
    (context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density).toInt()

fun getScreenRotation(context: Context): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display?.rotation
    } else {
        (context.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
    }

fun getScreenOrientation(context: Context): Int =
    context.resources.configuration.orientation
