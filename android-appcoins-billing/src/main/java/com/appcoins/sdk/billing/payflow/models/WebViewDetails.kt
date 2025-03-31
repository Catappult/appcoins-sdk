package com.appcoins.sdk.billing.payflow.models

import org.json.JSONObject
import java.io.Serializable

data class WebViewDetails(
    var forcedScreenOrientation: Int? = null,
    var landscapeScreenDimensions: OrientedScreenDimensions? = null,
    var portraitScreenDimensions: OrientedScreenDimensions? = null
) : Serializable {

    fun hasLandscapeDetails(): Boolean =
        landscapeScreenDimensions != null &&
            (
                landscapeScreenDimensions?.heightDp != null ||
                    landscapeScreenDimensions?.heightPercentage != null
                ) &&
            (landscapeScreenDimensions?.widthDp != null || landscapeScreenDimensions?.widthPercentage != null)

    fun hasPortraitDetails(): Boolean =
        portraitScreenDimensions != null &&
            (
                portraitScreenDimensions?.heightDp != null ||
                    portraitScreenDimensions?.heightPercentage != null
                ) &&
            (portraitScreenDimensions?.widthDp != null || portraitScreenDimensions?.widthPercentage != null)

    data class OrientedScreenDimensions(
        var widthDp: Int?,
        var heightDp: Int?,
        var widthPercentage: Double?,
        var heightPercentage: Double?,
    ) : Serializable

    companion object {
        fun fromJsonObject(screenDetailsJSONObject: JSONObject?): WebViewDetails? =
            screenDetailsJSONObject?.let {
                val forcedScreenOrientation =
                    screenDetailsJSONObject.optInt("force_screen_orientation").takeIf { it != 0 }

                var landscapeScreenDimensions: OrientedScreenDimensions? = null
                screenDetailsJSONObject.optJSONObject("landscape")?.let { jsonObject ->
                    val widthDp = jsonObject.optInt("width_dp").takeIf { it != 0 }
                    val heightDp = jsonObject.optInt("height_dp").takeIf { it != 0 }
                    val widthPercentage =
                        jsonObject.optDouble("width_percentage").takeIf { !it.isNaN() }
                    val heightPercentage =
                        jsonObject.optDouble("height_percentage")
                            .takeIf { !it.isNaN() }
                    landscapeScreenDimensions =
                        OrientedScreenDimensions(
                            widthDp,
                            heightDp,
                            widthPercentage,
                            heightPercentage
                        )
                }

                var portraitScreenDimensions: OrientedScreenDimensions? = null
                screenDetailsJSONObject.optJSONObject("portrait")?.let { jsonObject ->
                    val widthDp = jsonObject.optInt("width_dp").takeIf { it != 0 }
                    val heightDp = jsonObject.optInt("height_dp").takeIf { it != 0 }
                    val widthPercentage =
                        jsonObject.optDouble("width_percentage").takeIf { !it.isNaN() }
                    val heightPercentage =
                        jsonObject.optDouble("height_percentage")
                            .takeIf { !it.isNaN() }
                    portraitScreenDimensions =
                        OrientedScreenDimensions(
                            widthDp,
                            heightDp,
                            widthPercentage,
                            heightPercentage
                        )
                }

                WebViewDetails(forcedScreenOrientation, landscapeScreenDimensions, portraitScreenDimensions)
            }
    }
}
