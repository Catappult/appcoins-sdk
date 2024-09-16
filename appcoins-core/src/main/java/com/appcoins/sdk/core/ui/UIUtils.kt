package com.appcoins.sdk.core.ui

import android.content.Context
import android.util.TypedValue

fun floatToDps(value: Float, context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics)

fun getScreenHeightInDp(context: Context): Int =
    context.resources.configuration.screenHeightDp
