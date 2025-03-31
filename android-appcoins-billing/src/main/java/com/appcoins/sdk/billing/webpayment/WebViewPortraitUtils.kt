package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.appcoins.sdk.billing.payflow.models.WebViewDetails
import com.appcoins.sdk.billing.webpayment.WebViewGeneralUIUtils.applyDynamicConstraints
import com.appcoins.sdk.billing.webpayment.WebViewGeneralUIUtils.resetConstraintsAndSize
import com.appcoins.sdk.billing.webpayment.WebViewGeneralUIUtils.setMaxHeightForWebView
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.ui.floatToPxs
import com.appcoins.sdk.core.ui.getScreenOrientedHeightInDp

internal object WebViewPortraitUtils {

    private const val PORTRAIT_MAX_HEIGHT_DP = 560f

    fun applyDefaultPortraitConstraints(
        activity: Activity,
        mBaseConstraintLayout: ConstraintLayout,
        webViewContainerParams: ViewGroup.LayoutParams,
    ) {
        logError("Setting Default Portrait Constraints.")
        val mConstraintSet = ConstraintSet()
        mConstraintSet.clone(mBaseConstraintLayout)

        resetConstraintsAndSize(mConstraintSet, mBaseConstraintLayout, webViewContainerParams)

        val screenMaxHeight = getScreenOrientedHeightInDp(activity)

        if (screenMaxHeight < PORTRAIT_MAX_HEIGHT_DP) {
            setMaxHeightForWebView(mConstraintSet, mBaseConstraintLayout)
        } else {
            webViewContainerParams.height = floatToPxs(PORTRAIT_MAX_HEIGHT_DP, activity).toInt()
        }

        webViewContainerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
    }

    fun applyDynamicPortraitConstraints(
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
        ) { applyDefaultPortraitConstraints(activity, mBaseConstraintLayout, webViewContainerParams) }
    }
}
