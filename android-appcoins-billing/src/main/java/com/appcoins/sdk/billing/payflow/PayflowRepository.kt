package com.appcoins.sdk.billing.payflow

import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PayflowRepository(private val bdsService: BdsService) {

  fun getPayflowPriority(
    packageName: String,
    packageVersionCode: Int,
    sdkVersionCode: Int,
    walletVersionCode: Int?,
    gamesHubVersionCode: Int?,
    vanillaVersionCode: Int?,
    locale: String?,
    oemId: String?,
    guestWalletId: String?,
    billingFlowParams: BillingFlowParams?,
  ) : List<PaymentFlowMethod>? {
    val countDownLatch = CountDownLatch(1)
    var paymentFlowMethodList: List<PaymentFlowMethod>? = null

    val queries: MutableMap<String, String> = LinkedHashMap()
    queries["package"] = packageName
    queries["package_vercode"] = packageVersionCode.toString()
    queries["sdk_vercode"] = sdkVersionCode.toString()
    walletVersionCode?.let { queries["wallet_vercode"] = it.toString() }
    gamesHubVersionCode?.let { queries["gh_vercode"] = it.toString() }
    vanillaVersionCode?.let { queries["vanilla_vercode"] = it.toString() }
    locale?.let { queries["locale"] = it }
    oemId?.let { queries["oemid"] = it }
    guestWalletId?.let { queries["guest_wallet_id"] = it }
    billingFlowParams?.apply {
      sku?.let { queries["sku"] = it }
      developerPayload?.let { queries["metadata"] = it }
      orderReference?.let { queries["order_id"] = it }
    }

    val serviceResponseListener =
      ServiceResponseListener { requestResponse ->
        val payflowMethodResponse = PayflowResponseMapper().map(requestResponse)
        payflowMethodResponse.responseCode?.let { responseCode ->
          if (ServiceUtils.isSuccess(responseCode)) {
            val sortedMethods = payflowMethodResponse.paymentFlowList?.sortedBy { it.priority }
            paymentFlowMethodList = sortedMethods
          }
        }
        countDownLatch.countDown()
      }
    bdsService.makeRequest(
      "/payment_flow", "GET", emptyList(), queries, emptyMap(), emptyMap(), serviceResponseListener
    )

    waitForCountDown(countDownLatch)
    return paymentFlowMethodList
  }

  fun getPayflowPriorityAsync(
    payflowListener: PayflowListener,
    packageName: String,
    packageVersionCode: Int,
    sdkVersionCode: Int,
    walletVersionCode: Int?,
    gamesHubVersionCode: Int?,
    vanillaVersionCode: Int?,
    locale: String?,
    oemId: String?,
    guestWalletId: String?,
    billingFlowParams: BillingFlowParams?,
  ) {
    val queries: MutableMap<String, String> = LinkedHashMap()
    queries["package"] = packageName
    queries["package_vercode"] = packageVersionCode.toString()
    queries["sdk_vercode"] = sdkVersionCode.toString()
    walletVersionCode?.let { queries["wallet_vercode"] = it.toString() }
    gamesHubVersionCode?.let { queries["gh_vercode"] = it.toString() }
    vanillaVersionCode?.let { queries["vanilla_vercode"] = it.toString() }
    locale?.let { queries["locale"] = it }
    oemId?.let { queries["oemid"] = it }
    guestWalletId?.let { queries["guest_wallet_id"] = it }
    billingFlowParams?.apply {
      sku?.let { queries["sku"] = it }
      developerPayload?.let { queries["metadata"] = it }
      orderReference?.let { queries["order_id"] = it }
    }

    val serviceResponseListener =
      ServiceResponseListener { requestResponse ->
        payflowListener.onResponse(PayflowResponseMapper().map(requestResponse))
      }
    bdsService.makeRequest(
      "/payment_flow", "GET", emptyList(), queries, emptyMap(), emptyMap(), serviceResponseListener
    )
  }

  private fun waitForCountDown(countDownLatch: CountDownLatch) {
    try {
      countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
    } catch (e: InterruptedException) {
      e.printStackTrace()
    }
  }
}