package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
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
        webViewContainerParams.width = 0
        webViewContainerParams.height = 0

        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

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
        webViewContainerParams.width = 0
        webViewContainerParams.height = 0

        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        var isHeightSet: Boolean
        var isWidthSet: Boolean
        val screenHeight = getScreenOrientedHeightInDp(activity)
        val screenWidth = getScreenOrientedWidthInDp(activity)

        isHeightSet = handleExactHeight(activity, mConstraintSet, webViewDetails, screenHeight)

        isWidthSet = handleExactWidth(activity, mConstraintSet, webViewDetails, screenWidth)

        if (isHeightSet && isWidthSet) {
            mConstraintSet.applyTo(mBaseConstraintLayout)
            return
        }

        isHeightSet = handlePercentageHeight(mConstraintSet, webViewDetails, isHeightSet)

        if (isHeightSet && isWidthSet) {
            mConstraintSet.applyTo(mBaseConstraintLayout)
            return
        }

        isWidthSet = handlePercentageWidth(mConstraintSet, webViewDetails, isWidthSet)

        if (isHeightSet && isWidthSet) {
            mConstraintSet.applyTo(mBaseConstraintLayout)
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
            mConstraintSet.constrainHeight(
                R.id.container_for_web_view,
                floatToPxs(heightDp.toFloat(), activity).toInt()
            )
            return true
        }
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
            mConstraintSet.constrainWidth(
                R.id.container_for_web_view,
                floatToPxs(widthDp.toFloat(), activity).toInt()
            )
            return true
        }
        return false
    }

    private fun handlePercentageHeight(
        mConstraintSet: ConstraintSet,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails,
        isHeightSet: Boolean
    ): Boolean {
        if (isHeightSet) {
            return true
        }

        val heightPercentage = webViewDetails.landscapeScreenDimensions?.heightPercentage
        if (heightPercentage != null && heightPercentage in 0.0..1.0) {
            mConstraintSet.constrainPercentHeight(R.id.container_for_web_view, heightPercentage.toFloat())
            return true
        }
        return false
    }

    private fun handlePercentageWidth(
        mConstraintSet: ConstraintSet,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails,
        isWidthSet: Boolean
    ): Boolean {
        if (isWidthSet) {
            return true
        }

        val widthPercentage = webViewDetails.landscapeScreenDimensions?.widthPercentage
        if (widthPercentage != null && widthPercentage in 0.0..1.0) {
            mConstraintSet.constrainPercentWidth(R.id.container_for_web_view, widthPercentage.toFloat())
            return true
        }
        return false
    }
}
