package com.appcoins.sdk.billing.webpayment

import android.app.Activity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.core.ui.floatToPxs
import com.appcoins.sdk.core.ui.getScreenHeightInDp

internal object WebViewPortraitUtils {

    private const val PORTRAIT_MAX_HEIGHT_DP = 560f

    fun applyDynamicPortraitConstraints(
        activity: Activity,
        webViewContainerParams: ViewGroup.LayoutParams,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails
    ) {
        if (exactScreenDimensionsAvailable(webViewDetails)) {
            applyExactScreenDimensions(activity, webViewContainerParams, webViewDetails)
            return
        }

        if (percentageScreenDimensionsAvailable(webViewDetails)) {
            applyPercentageScreenDimensions(activity, webViewContainerParams, webViewDetails)
            return
        }

        applyDefaultPortraitConstraints(activity, webViewContainerParams)
    }

    fun applyDefaultPortraitConstraints(activity: Activity, webViewContainerParams: ViewGroup.LayoutParams) {
        val screenMaxHeight = getScreenHeightInDp(activity)
        val heightToSet =
            if (screenMaxHeight < PORTRAIT_MAX_HEIGHT_DP) {
                LinearLayout.LayoutParams.MATCH_PARENT
            } else {
                floatToPxs(PORTRAIT_MAX_HEIGHT_DP, activity).toInt()
            }
        webViewContainerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        webViewContainerParams.height = heightToSet
    }

    private fun exactScreenDimensionsAvailable(webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails): Boolean =
        webViewDetails.portraitScreenDimensions?.widthDp != null ||
            webViewDetails.portraitScreenDimensions?.heightDp != null

    private fun percentageScreenDimensionsAvailable(webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails): Boolean =
        webViewDetails.portraitScreenDimensions?.widthPercentage != null ||
            webViewDetails.portraitScreenDimensions?.heightPercentage != null

    private fun applyExactScreenDimensions(
        activity: Activity,
        webViewContainerParams: ViewGroup.LayoutParams,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails
    ) {

    }

    private fun applyPercentageScreenDimensions(
        activity: Activity,
        webViewContainerParams: ViewGroup.LayoutParams,
        webViewDetails: PaymentFlowMethod.WebPayment.WebViewDetails
    ) {

    }
}