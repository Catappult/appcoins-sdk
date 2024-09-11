package com.appcoins.sdk.billing.mappers

import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.utils.ServiceUtils.isSuccess
import com.appcoins.sdk.core.logger.Logger.logError
import org.json.JSONObject

class AppVersionResponseMapper {
    fun map(response: RequestResponse): AppVersionResponse {
        if (!isSuccess(response.responseCode) || response.response == null) {
            logError("Failed to obtain AppVersion Response. ResponseCode: ${response.responseCode} | Cause: ${response.exception}")
            WalletUtils.getSdkAnalytics()
                .sendCallBackendAppVersionEvent(
                    response.responseCode,
                    null,
                    response.exception?.toString()
                )
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
            logError("There was a an error mapping the response.", Exception(it))
            AppVersionResponse(response.responseCode)
        }

        WalletUtils.getSdkAnalytics()
            .sendCallBackendAppVersionEvent(response.responseCode, appVersionResponse.toString())

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
