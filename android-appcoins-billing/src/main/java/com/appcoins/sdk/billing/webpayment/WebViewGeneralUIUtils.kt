package com.appcoins.sdk.billing.webpayment

import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import com.appcoins.billing.sdk.R

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
}
