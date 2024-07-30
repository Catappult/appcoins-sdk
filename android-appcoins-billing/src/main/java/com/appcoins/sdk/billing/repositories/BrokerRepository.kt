package com.appcoins.sdk.billing.repositories

import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.listeners.PurchasesListener
import com.appcoins.sdk.billing.listeners.PurchasesModel
import com.appcoins.sdk.billing.listeners.billing.PurchaseListener
import com.appcoins.sdk.billing.listeners.billing.TransactionsListener
import com.appcoins.sdk.billing.mappers.PurchaseMapper
import com.appcoins.sdk.billing.mappers.TransactionMapper
import com.appcoins.sdk.billing.mappers.TransactionResponse
import com.appcoins.sdk.billing.mappers.TransactionResponseMapper
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.service.RequestResponse
import com.appcoins.sdk.billing.service.ServiceResponseListener
import com.appcoins.sdk.billing.utils.EnumMapper
import com.appcoins.sdk.billing.utils.ServiceUtils
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class BrokerRepository(private val bdsService: BdsService) {

    fun getTransaction(orderId: String): TransactionResponse? {
        val countDownLatch = CountDownLatch(1)
        var transaction: TransactionResponse? = null

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                requestResponse?.let {
                    val transactionResponse = TransactionResponseMapper().map(requestResponse)
                    transactionResponse.responseCode?.let { responseCode ->
                        if (ServiceUtils.isSuccess(responseCode)) {
                            transaction = transactionResponse
                        }
                    }
                }
                countDownLatch.countDown()
            }

        bdsService.makeRequest(
            "/broker/8.20240722/transactions/$orderId",
            "GET",
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap<String?, Any>(),
            serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return transaction
    }

    fun getSkuPurchase(
        packageName: String, sku: String, walletAddress: String, walletSignature: String,
        purchaseListener: PurchaseListener
    ) {
        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val purchaseMapper = PurchaseMapper()
                val purchaseModel = purchaseMapper.map(requestResponse)
                purchaseListener.onResponse(purchaseModel)
            }

        val path: MutableList<String> = ArrayList()
        path.add(packageName)
        path.add("products")
        path.add(sku)
        path.add("purchase")

        val queries: MutableMap<String, String> = HashMap()
        queries["wallet.address"] = walletAddress
        queries["wallet.signature"] = walletSignature
        bdsService.makeRequest(
            "/inapp/8.20180518/packages", "GET", path, queries, null, null,
            serviceResponseListener
        )
    }

    fun getPurchases(
        packageName: String, walletAddress: String, signedWallet: String,
        type: String, purchasesListener: PurchasesListener
    ) {
        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val purchaseMapper = PurchaseMapper()
                val purchasesModel = purchaseMapper.mapList(requestResponse)
                purchasesListener.onResponse(purchasesModel)
            }

        val path: MutableList<String> = ArrayList()
        path.add(packageName)
        path.add("purchases")

        val queries: MutableMap<String, String> = HashMap()
        queries["wallet.address"] = walletAddress
        queries["wallet.signature"] = signedWallet
        queries["type"] = type.lowercase(Locale.getDefault())

        bdsService.makeRequest(
            "/inapp/8.20180518/packages", "GET", path, queries,
            HashMap<String, String>(), HashMap<String, Any>(), serviceResponseListener
        )
    }

    fun getPurchasesSync(
        packageName: String, walletAddress: String,
        signedWallet: String, type: String
    ): PurchasesModel {
        val countDownLatch = CountDownLatch(1)
        val purchasesModelArray = arrayOf(PurchasesModel())

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val purchaseMapper = PurchaseMapper()
                val purchasesModel = purchaseMapper.mapList(requestResponse)
                purchasesModelArray[0] = purchasesModel
                countDownLatch.countDown()
            }

        val path: MutableList<String> = ArrayList()
        path.add(packageName)
        path.add("purchases")

        val queries: MutableMap<String, String> = HashMap()
        queries["wallet.address"] = walletAddress
        queries["wallet.signature"] = signedWallet
        queries["type"] = type

        bdsService.makeRequest(
            "/inapp/8.20180518/packages", "GET", path, queries,
            HashMap<String, String>(), HashMap<String, Any>(), serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return purchasesModelArray[0]
    }

    fun consumePurchaseSync(
        walletAddress: String, signature: String, packageName: String,
        purchaseToken: String
    ): Int {
        val countDownLatch = CountDownLatch(1)
        val responseCode = intArrayOf(ResponseCode.ERROR.value)

        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                handleConsumeResponse(
                    requestResponse,
                    countDownLatch,
                    responseCode
                )
            }
        val path: MutableList<String> = ArrayList()
        path.add(packageName)
        path.add("purchases")
        path.add(purchaseToken)

        val queries: MutableMap<String, String> = HashMap()
        queries["wallet.address"] = walletAddress
        queries["wallet.signature"] = signature

        val body: MutableMap<String, Any> = HashMap()
        body["status"] = "CONSUMED"

        bdsService.makeRequest(
            "/inapp/8.20180518/packages", "PATCH", path, queries,
            HashMap<String, String>(), body, serviceResponseListener
        )

        waitForCountDown(countDownLatch)
        return responseCode[0]
    }

    fun getSkuTransaction(
        address: String, signature: String, skuId: String?, packageName: String,
        transactionsListener: TransactionsListener
    ) {
        val serviceResponseListener =
            ServiceResponseListener { requestResponse ->
                val transactionMapper = TransactionMapper(EnumMapper())
                val transactionsListModel =
                    transactionMapper.mapTransactionListResponse(requestResponse)
                transactionsListener.onResponse(transactionsListModel)
            }
        val queries: MutableMap<String, String> = HashMap()
        queries["wallet.address"] = address
        queries["wallet.signature"] = signature
        queries["cursor"] = "0"
        queries["type"] = "INAPP"
        queries["limit"] = "1"
        queries["sort.name"] = "latest"
        queries["sort.reverse"] = "false"
        if (skuId != null) queries["product"] = skuId
        queries["domain"] = packageName

        bdsService.makeRequest(
            "/broker/8.20180518/transactions", "GET", ArrayList<String>(), queries,
            HashMap<String, String>(), HashMap<String, Any>(), serviceResponseListener
        )
    }

    private fun handleConsumeResponse(
        requestResponse: RequestResponse, countDownLatch: CountDownLatch,
        responseCode: IntArray
    ) {
        if (ServiceUtils.isSuccess(requestResponse.responseCode)) {
            responseCode[0] = ResponseCode.OK.value
        } else {
            responseCode[0] = ResponseCode.ERROR.value
        }
        countDownLatch.countDown()
    }

    private fun waitForCountDown(countDownLatch: CountDownLatch) {
        try {
            countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
