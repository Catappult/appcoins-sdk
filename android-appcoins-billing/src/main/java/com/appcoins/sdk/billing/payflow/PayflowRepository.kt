package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener

class PayflowRepository(private val bdsService: BdsService) {

  fun getPayflowPriority(
    payflowListener: PayflowListener,
    packageName: String,
    packageVersionCode: Int,
    sdkVersionCode: Int,
    walletVersionCode: Int?,
    gamesHubVersionCode: Int?,
    vanillaVersionCode: Int?,
    locale: String?,
  ) {
    val queries: MutableMap<String, String> = LinkedHashMap()
    queries["package"] = packageName
    queries["package_vercode"] = packageVersionCode.toString()
    queries["sdk_vercode"] = sdkVersionCode.toString()
    walletVersionCode?.let { queries["wallet_vercode"] = it.toString() }
    gamesHubVersionCode?.let { queries["gh_vercode"] = it.toString() }
    vanillaVersionCode?.let { queries["vanilla_vercode"] = it.toString() }
    locale?.let { queries["locale"] = it }

    val serviceResponseListener =
      ServiceResponseListener { requestResponse ->
        payflowListener.onResponse(PayflowResponseMapper().map(requestResponse))
      }
    bdsService.makeRequest(
      "/payment_flow", "GET", emptyList(), queries, emptyMap(), emptyMap(), serviceResponseListener
    );
  }
}