package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.webpayment.WebViewGeneralUIUtils.resetConstraintsAndSize
import com.appcoins.sdk.core.ui.floatToPxs

internal object WebViewTabletUtils {

    fun applyTabletConstraints(
        activity: Activity,
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams
    ) {
        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        resetConstraintsAndSize(mConstraintSet, mBaseConstraintLayout, webViewContainerParams)

        mConstraintSet.constrainPercentHeight(
            R.id.container_for_web_view,
            TABLET_MAX_HEIGHT_PERCENT
        )
        mConstraintSet.constrainPercentWidth(
            R.id.container_for_web_view,
            TABLET_MAX_WIDTH_PERCENT
        )
        mConstraintSet.constrainMaxHeight(
            R.id.container_for_web_view,
            floatToPxs(TABLET_MAX_HEIGHT_DP, activity).toInt()
        )
        mConstraintSet.constrainMaxWidth(
            R.id.container_for_web_view,
            floatToPxs(TABLET_MAX_WIDTH_DP, activity).toInt()
        )

        mConstraintSet.applyTo(mBaseConstraintLayout)
    }

    private const val TABLET_MAX_HEIGHT_DP = 480f
    private const val TABLET_MAX_WIDTH_DP = 688f
    private const val TABLET_MAX_HEIGHT_PERCENT = 0.9f
    private const val TABLET_MAX_WIDTH_PERCENT = 0.9f
}
