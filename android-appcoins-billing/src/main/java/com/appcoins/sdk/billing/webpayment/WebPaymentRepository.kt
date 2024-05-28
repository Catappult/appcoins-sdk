package com.appcoins.sdk.billing.webpayment

import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebPaymentRepository(private val bdsService: BdsService) {

    fun getWebPaymentUrl(
        packageName: String,
        locale: String?,
        oemId: String?,
        guestWalletId: String?,
        billingFlowParams: BillingFlowParams?,
    ): String? {
        val countDownLatch = CountDownLatch(1)
        var webPaymentUrl: String? = null

        val queries: MutableMap<String, String> = LinkedHashMap()
        queries["package"] = packageName
        locale?.let { queries["locale"] = it }
        oemId?.let { queries["oemid"] = it }
        guestWalletId?.let { queries["guest_id"] = it }
        billingFlowParams?.apply {
            sku?.let { queries["sku"] = it }
            developerPayload?.let { queries["metadata"] = it }
            orderReference?.let { queries["order_id"] = it }
        }

        val paymentUrlVersion =
            PaymentFlowMethod
                .getPaymentUrlVersionFromPayflowMethod(WalletUtils.getPayflowMethodsList())

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val webPaymentUrlResponse = WebPaymentResponseMapper().map(requestResponse)
                webPaymentUrlResponse.responseCode?.let { responseCode ->
                    if (ServiceUtils.isSuccess(responseCode)) {
                        webPaymentUrl = webPaymentUrlResponse.webPaymentUrl
                    }
                }
                countDownLatch.countDown()
            }
        bdsService.makeRequest(
            "/payment_url/$paymentUrlVersion",
            "GET",
            emptyList(),
            queries,
            emptyMap(),
            emptyMap(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return webPaymentUrl
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}