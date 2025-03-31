package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.payflow.models.WebViewDetails
import com.appcoins.sdk.billing.webpayment.WebViewGeneralUIUtils.applyDynamicConstraints
import com.appcoins.sdk.billing.webpayment.WebViewGeneralUIUtils.resetConstraintsAndSize

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
        webViewDetailsDimensions: WebViewDetails.OrientedScreenDimensions?
    ) {
        applyDynamicConstraints(
            activity,
            mBaseConstraintLayout,
            webViewContainerParams,
            webViewDetailsDimensions
        ) { applyDefaultLandscapeConstraints(mBaseConstraintLayout, webViewContainerParams) }
    }
}
