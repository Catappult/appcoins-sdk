package com.appcoins.sdk.billing.webpayment

import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod

internal object WebViewLandscapeUtils {

    private const val LANDSCAPE_MAX_HEIGHT_PERCENT = 0.9f
    private const val LANDSCAPE_MAX_WIDTH_PERCENT = 0.9f

    fun applyDynamicLandscapeConstraints(
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails
    ) {
        if (exactScreenDimensionsAvailable(webViewDetails)) {
            applyExactScreenDimensions(mBaseConstraintLayout, webViewContainerParams, webViewDetails)
            return
        }

        if (percentageScreenDimensionsAvailable(webViewDetails)) {
            applyPercentageScreenDimensions(mBaseConstraintLayout, webViewContainerParams, webViewDetails)
            return
        }

        applyDefaultLandscapeConstraints(mBaseConstraintLayout, webViewContainerParams)
    }

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

    private fun exactScreenDimensionsAvailable(webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails): Boolean =
        webViewDetails.landscapeScreenDimensions?.widthDp != null ||
            webViewDetails.landscapeScreenDimensions?.heightDp != null

    private fun percentageScreenDimensionsAvailable(webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails): Boolean =
        webViewDetails.landscapeScreenDimensions?.widthPercentage != null ||
            webViewDetails.landscapeScreenDimensions?.heightPercentage != null

    private fun applyExactScreenDimensions(
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails
    ) {

    }

    private fun applyPercentageScreenDimensions(
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails
    ) {

    }
}