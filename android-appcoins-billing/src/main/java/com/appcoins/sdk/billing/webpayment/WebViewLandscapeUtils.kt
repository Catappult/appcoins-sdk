package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.billing.webpayment.WebViewGeneralUIUtils.resetConstraintsAndSize
import com.appcoins.sdk.billing.webpayment.WebViewGeneralUIUtils.setMaxHeightForWebView
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.ui.floatToPxs
import com.appcoins.sdk.core.ui.getScreenOrientedHeightInDp
import com.appcoins.sdk.core.ui.getScreenOrientedWidthInDp

internal object WebViewLandscapeUtils {

    private const val LANDSCAPE_MAX_HEIGHT_PERCENT = 0.9f
    private const val LANDSCAPE_MAX_WIDTH_PERCENT = 0.9f

    fun applyDefaultLandscapeConstraints(
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams
    ) {
        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        resetConstraintsAndSize(mConstraintSet, mBaseConstraintLayout, webViewContainerParams)

        mConstraintSet.constrainPercentHeight(
            R.id.container_for_web_view,
            LANDSCAPE_MAX_HEIGHT_PERCENT
        )
        mConstraintSet.constrainPercentWidth(
            R.id.container_for_web_view,
            LANDSCAPE_MAX_WIDTH_PERCENT
        )
        mConstraintSet.constrainMaxHeight(R.id.container_for_web_view, 0)
        mConstraintSet.constrainMaxWidth(R.id.container_for_web_view, 0)

        mConstraintSet.applyTo(mBaseConstraintLayout)
    }

    fun applyDynamicLandscapeConstraints(
        activity: Activity,
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails
    ) {
        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        resetConstraintsAndSize(mConstraintSet, mBaseConstraintLayout, webViewContainerParams)

        var isHeightSet: Boolean
        var isWidthSet: Boolean
        val screenHeight = getScreenOrientedHeightInDp(activity)
        val screenWidth = getScreenOrientedWidthInDp(activity)

        isHeightSet = handleExactHeight(activity, mConstraintSet, webViewDetails, screenHeight)

        isWidthSet = handleExactWidth(activity, mConstraintSet, webViewDetails, screenWidth)

        if (isHeightSet && isWidthSet) {
            logError("Sizes set in Landscape. Applying 1.")
            mConstraintSet.applyTo(mBaseConstraintLayout)
            return
        }

        isHeightSet = handlePercentageHeight(mConstraintSet, mBaseConstraintLayout, webViewDetails, isHeightSet)

        if (isHeightSet && isWidthSet) {
            logError("Sizes set in Landscape. Applying 2.")
            mConstraintSet.applyTo(mBaseConstraintLayout)
            return
        }

        isWidthSet = handlePercentageWidth(mConstraintSet, webViewDetails, isWidthSet)

        if (isHeightSet && isWidthSet) {
            logError("Sizes set in Landscape. Applying 3.")
            mConstraintSet.applyTo(mBaseConstraintLayout)
            mBaseConstraintLayout.requestLayout()
            return
        }

        applyDefaultLandscapeConstraints(mBaseConstraintLayout, webViewContainerParams)
    }

    private fun handleExactHeight(
        activity: Activity,
        mConstraintSet: ConstraintSet,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails,
        screenHeight: Int
    ): Boolean {
        val heightDp = webViewDetails.landscapeScreenDimensions?.heightDp
        if (heightDp != null && heightDp < screenHeight) {
            logError("Landscape will handle exact Height. $heightDp")
            mConstraintSet.constrainHeight(
                R.id.container_for_web_view,
                floatToPxs(heightDp.toFloat(), activity).toInt()
            )
            return true
        }
        logError("Landscape NOT handling exact Height.")
        return false
    }

    private fun handleExactWidth(
        activity: Activity,
        mConstraintSet: ConstraintSet,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails,
        screenWidth: Int
    ): Boolean {
        val widthDp = webViewDetails.landscapeScreenDimensions?.widthDp
        if (widthDp != null && widthDp < screenWidth) {
            logError("Landscape will handle exact Width. $widthDp")
            mConstraintSet.constrainWidth(
                R.id.container_for_web_view,
                floatToPxs(widthDp.toFloat(), activity).toInt()
            )
            return true
        }
        logError("Landscape NOT handling exact Width.")
        return false
    }

    private fun handlePercentageHeight(
        mConstraintSet: ConstraintSet,
        mBaseConstraintLayout: ConstraintLayout,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails,
        isHeightSet: Boolean
    ): Boolean {
        if (isHeightSet) {
            logError("Landscape Height already set.")
            return true
        }

        val heightPercentage = webViewDetails.landscapeScreenDimensions?.heightPercentage
        if (heightPercentage != null && heightPercentage in 0.0..1.0) {
            logError("Landscape will handle percentage Height. ${heightPercentage.toFloat()}")
            if (heightPercentage == 1.0) {
                setMaxHeightForWebView(mConstraintSet, mBaseConstraintLayout)
            } else {
                mConstraintSet.constrainPercentHeight(R.id.container_for_web_view, heightPercentage.toFloat())
            }
            return true
        }
        logError("Landscape NOT handling percentage Height.")
        return false
    }

    private fun handlePercentageWidth(
        mConstraintSet: ConstraintSet,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails,
        isWidthSet: Boolean
    ): Boolean {
        if (isWidthSet) {
            logError("Landscape Width already set.")
            return true
        }

        val widthPercentage = webViewDetails.landscapeScreenDimensions?.widthPercentage
        if (widthPercentage != null && widthPercentage in 0.0..1.0) {
            logError("Landscape will handle percentage Width. ${widthPercentage.toFloat()}")
            mConstraintSet.constrainPercentWidth(R.id.container_for_web_view, widthPercentage.toFloat())
            return true
        }
        logError("Landscape NOT handling percentage Width.")
        return false
    }
}
