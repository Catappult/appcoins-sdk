package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.core.ui.floatToPxs
import com.appcoins.sdk.core.ui.getScreenOrientedHeightInDp
import com.appcoins.sdk.core.ui.getScreenOrientedWidthInDp

internal object WebViewPortraitUtils {

    private const val PORTRAIT_MAX_HEIGHT_DP = 560f

    fun applyDefaultPortraitConstraints(activity: Activity, webViewContainerParams: ViewGroup.LayoutParams) {
        val screenMaxHeight = getScreenOrientedHeightInDp(activity)
        val heightToSet =
            if (screenMaxHeight < PORTRAIT_MAX_HEIGHT_DP) {
                LinearLayout.LayoutParams.MATCH_PARENT
            } else {
                floatToPxs(PORTRAIT_MAX_HEIGHT_DP, activity).toInt()
            }
        webViewContainerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        webViewContainerParams.height = heightToSet
    }

    fun applyDynamicPortraitConstraints(
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

        applyDefaultPortraitConstraints(activity, webViewContainerParams)
    }

    private fun handleExactHeight(
        activity: Activity,
        mConstraintSet: ConstraintSet,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails,
        screenHeight: Int
    ): Boolean {
        val heightDp = webViewDetails.portraitScreenDimensions?.heightDp
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
        val widthDp = webViewDetails.portraitScreenDimensions?.widthDp
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

        val heightPercentage = webViewDetails.portraitScreenDimensions?.heightPercentage
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

        val widthPercentage = webViewDetails.portraitScreenDimensions?.widthPercentage
        if (widthPercentage != null && widthPercentage in 0.0..1.0) {
            mConstraintSet.constrainPercentWidth(R.id.container_for_web_view, widthPercentage.toFloat())
            return true
        }
        return false
    }
}
