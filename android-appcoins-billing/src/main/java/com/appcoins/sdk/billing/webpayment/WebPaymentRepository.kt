package com.appcoins.sdk.billing.webpayment

import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.ServiceUtils
import com.appcoins.sdk.core.logger.Logger.logError
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebPaymentRepository(private val bdsService: BdsService) {

    fun getWebPaymentUrl(
        packageName: String,
        locale: String?,
        oemId: String?,
        walletId: String?,
        billingFlowParams: BillingFlowParams?,
    ): String? {
        val countDownLatch = CountDownLatch(1)
        var webPaymentUrl: String? = null

        val paymentFlow =
            PaymentFlowMethod.getPaymentFlowFromPayflowMethod(WalletUtils.getPayflowMethodsList())

        val queries: MutableMap<String, String> = LinkedHashMap()
        queries["package"] = packageName
        locale?.let { queries["locale"] = it }
        oemId?.let { queries["oemid"] = it }
        walletId?.let { queries["guest_id"] = it }
        billingFlowParams?.apply {
            sku?.let { queries["sku"] = it }
            developerPayload?.let { queries["metadata"] = it }
            orderReference?.let { queries["order_id"] = it }
        }
        paymentFlow?.let { queries["payment_flow"] = it }
        queries["lang_code"] = Locale.getDefault().language

        val paymentUrlVersion =
            PaymentFlowMethod.getPaymentUrlVersionFromPayflowMethod(WalletUtils.getPayflowMethodsList())

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
            "/$paymentUrlVersion/payment_url",
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
            logError("Timeout for WebPaymentUrl request: $e")
        }
    }
}