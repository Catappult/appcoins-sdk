package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.payflow.models.WebViewDetails
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.logger.Logger.logWarning
import com.appcoins.sdk.core.ui.floatToPxs
import com.appcoins.sdk.core.ui.getScreenOrientedHeightInDp
import com.appcoins.sdk.core.ui.getScreenOrientedWidthInDp

internal object WebViewGeneralUIUtils {

    fun resetConstraintsAndSize(
        mConstraintSet: ConstraintSet,
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams
    ) {
        webViewContainerParams.width = 0
        webViewContainerParams.height = 0

        mConstraintSet.clear(R.id.container_for_web_view)
        mConstraintSet.connect(
            R.id.container_for_web_view,
            ConstraintSet.BOTTOM,
            mBaseConstraintLayout.id,
            ConstraintSet.BOTTOM,
            0
        )
        mConstraintSet.connect(
            R.id.container_for_web_view,
            ConstraintSet.START,
            mBaseConstraintLayout.id,
            ConstraintSet.START,
            0
        )
        mConstraintSet.connect(
            R.id.container_for_web_view,
            ConstraintSet.END,
            mBaseConstraintLayout.id,
            ConstraintSet.END,
            0
        )
    }

    /* Manually applied vertically centered constraint since the WebView
    inside of the container_for_web_view breaks breaks the 1.0 heightPercent.
     */
    fun setMaxHeightForWebView(mConstraintSet: ConstraintSet, mBaseConstraintLayout: ConstraintLayout) {
        mConstraintSet.constrainHeight(R.id.container_for_web_view, 0)
        mConstraintSet.connect(
            R.id.container_for_web_view,
            ConstraintSet.TOP,
            mBaseConstraintLayout.id,
            ConstraintSet.TOP,
            0
        )
    }

    fun applyDynamicConstraints(
        activity: Activity,
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams,
        webViewDetailsDimensions: WebViewDetails.OrientedScreenDimensions?,
        defaultFallback: () -> Unit
    ) {
        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        resetConstraintsAndSize(mConstraintSet, mBaseConstraintLayout, webViewContainerParams)

        var isHeightSet: Boolean
        var isWidthSet: Boolean
        val screenHeight = getScreenOrientedHeightInDp(activity)
        val screenWidth = getScreenOrientedWidthInDp(activity)

        isHeightSet = handleExactHeight(activity, mConstraintSet, webViewDetailsDimensions, screenHeight)

        isWidthSet = handleExactWidth(activity, mConstraintSet, webViewDetailsDimensions, screenWidth)

        if (isHeightSet && isWidthSet) {
            logInfo("Height and Width set on 1st condition.")
            mConstraintSet.applyTo(mBaseConstraintLayout)
            return
        }

        isHeightSet =
            handlePercentageHeight(mConstraintSet, mBaseConstraintLayout, webViewDetailsDimensions, isHeightSet)

        if (isHeightSet && isWidthSet) {
            logInfo("Height and Width set on 2nd condition.")
            mConstraintSet.applyTo(mBaseConstraintLayout)
            return
        }

        isWidthSet = handlePercentageWidth(mConstraintSet, webViewDetailsDimensions, isWidthSet)

        if (isHeightSet && isWidthSet) {
            logInfo("Height and Width set on 3rd condition.")
            mConstraintSet.applyTo(mBaseConstraintLayout)
            return
        }

        defaultFallback()
    }

    private fun handleExactHeight(
        activity: Activity,
        mConstraintSet: ConstraintSet,
        webViewDetailsDimensions: WebViewDetails.OrientedScreenDimensions?,
        screenHeight: Int
    ): Boolean {
        val heightDp = webViewDetailsDimensions?.heightDp
        if (heightDp != null && heightDp < screenHeight) {
            logInfo("Handling exact Height.")
            mConstraintSet.constrainHeight(
                R.id.container_for_web_view,
                floatToPxs(heightDp.toFloat(), activity).toInt()
            )
            return true
        }
        logWarning("Failed to handle exact Height.")
        return false
    }

    private fun handleExactWidth(
        activity: Activity,
        mConstraintSet: ConstraintSet,
        webViewDetailsDimensions: WebViewDetails.OrientedScreenDimensions?,
        screenWidth: Int
    ): Boolean {
        val widthDp = webViewDetailsDimensions?.widthDp
        if (widthDp != null && widthDp < screenWidth) {
            logInfo("Handling exact Width.")
            mConstraintSet.constrainWidth(
                R.id.container_for_web_view,
                floatToPxs(widthDp.toFloat(), activity).toInt()
            )
            return true
        }
        logWarning("Failed to handle exact Width.")
        return false
    }

    private fun handlePercentageHeight(
        mConstraintSet: ConstraintSet,
        mBaseConstraintLayout: ConstraintLayout,
        webViewDetailsDimensions: WebViewDetails.OrientedScreenDimensions?,
        isHeightSet: Boolean
    ): Boolean {
        if (isHeightSet) {
            logInfo("Height already set.")
            return true
        }

        val heightPercentage = webViewDetailsDimensions?.heightPercentage
        if (heightPercentage != null && heightPercentage in 0.0..1.0) {
            logInfo("Handling percentage Height.")
            if (heightPercentage == 1.0) {
                setMaxHeightForWebView(mConstraintSet, mBaseConstraintLayout)
            } else {
                mConstraintSet.constrainPercentHeight(R.id.container_for_web_view, heightPercentage.toFloat())
            }
            return true
        }
        logWarning("Failed to handle percentage Height.")
        return false
    }

    private fun handlePercentageWidth(
        mConstraintSet: ConstraintSet,
        webViewDetailsDimensions: WebViewDetails.OrientedScreenDimensions?,
        isWidthSet: Boolean
    ): Boolean {
        if (isWidthSet) {
            logInfo("Width already set.")
            return true
        }

        val widthPercentage = webViewDetailsDimensions?.widthPercentage
        if (widthPercentage != null && widthPercentage in 0.0..1.0) {
            logInfo("Handling percentage Width.")
            mConstraintSet.constrainPercentWidth(R.id.container_for_web_view, widthPercentage.toFloat())
            return true
        }
        logWarning("Failed to handle percentage Width.")
        return false
    }
}
