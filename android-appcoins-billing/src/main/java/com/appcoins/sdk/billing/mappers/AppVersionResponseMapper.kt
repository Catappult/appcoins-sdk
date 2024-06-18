package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import org.json.JSONObject

class AppVersionResponseMapper {
    fun map(response: RequestResponse): AppVersionResponse {
        /*WalletUtils.getSdkAnalytics()
            .sendCallBackendWebPaymentUrlEvent(response.responseCode, response.response)*/

        if (!isSuccess(response.responseCode) || response.response == null) {
            return AppVersionResponse(response.responseCode)
        }

        val appVersionResponse = runCatching {
            val jsonObjectResponse = JSONObject(response.response)
            val mutableList = mutableListOf<Version>()

            jsonObjectResponse.optJSONObject("nodes")?.let { nodesJson ->
                nodesJson.optJSONObject("versions")?.let { versionsJson ->
                    versionsJson.optJSONArray("list")?.let { versionsListJson ->
                        for (i in 0 until versionsListJson.length()) {
                            versionsListJson.optJSONObject(i)?.let { versionJson ->
                                versionJson.optJSONObject("file")?.let { fileJson ->
                                    fileJson.optJSONObject("hardware")?.let { hardwareJson ->
                                        val vercode = fileJson.optInt("vercode")
                                        val minSdk = hardwareJson.optInt("sdk")

                                        mutableList.add(Version(vercode, minSdk))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AppVersionResponse(response.responseCode, mutableList)
        }.getOrElse {
            it.printStackTrace()
            AppVersionResponse(response.responseCode)
        }
        return appVersionResponse
    }
}

data class AppVersionResponse(
    val responseCode: Int?,
    val versions: List<Version>? = null,
)

data class Version(
    val versionCode: Int,
    val minSdk: Int,
)
