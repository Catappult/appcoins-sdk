package com.appcoins.sdk.billing.helpers

import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.BuyItemProperties
import com.appcoins.sdk.billing.DeveloperPayload
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.SkuDetails
import com.appcoins.sdk.billing.SkuDetailsResult
import com.appcoins.sdk.billing.WSServiceController
import com.appcoins.sdk.billing.payasguest.BillingRepository
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.Companion.getPaymentFlowFromPayflowMethod
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.PayAsAGuest
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.WebPayment
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.webpayment.WebPaymentManager
import java.io.Serializable
import java.util.concurrent.CountDownLatch

class WebAppcoinsBilling private constructor() : AppcoinsBilling, Serializable {

    init {
        webAppcoinsBilling = this
    }

    override fun asBinder(): IBinder? = null

    override fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int {
        var responseCode = ResponseCode.SERVICE_UNAVAILABLE.value
        if (isTypeSupported(type, apiVersion)) {
            responseCode = ResponseCode.OK.value
        }
        return responseCode
    }

    override fun getSkuDetails(
        apiVersion: Int,
        packageName: String,
        type: String,
        skusBundle: Bundle
    ): Bundle {
        val latch = CountDownLatch(1)
        val responseWs = Bundle()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Thread {
                getSkuDetailsFromService(packageName, type, skusBundle, responseWs)
                latch.countDown()
            }.start()
            try {
                latch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                responseWs.putInt(Utils.RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.value)
            }
        } else {
            getSkuDetailsFromService(packageName, type, skusBundle, responseWs)
        }
        return responseWs
    }

    override fun getBuyIntent(
        apiVersion: Int,
        packageName: String,
        sku: String,
        type: String,
        developerPayload: String
    ): Bundle {
        var bundle: Bundle? = null
        if (hasRequiredFields(type, sku) && WalletUtils.getPayflowMethodsList().isNotEmpty()) {
            for (method in WalletUtils.getPayflowMethodsList()) {
                if (method is PayAsAGuest) {
                    Log.d(
                        TAG,
                        "Service is NOT installed and should StartPayAsGuest with buyItemProperties = [$buyItemProperties]"
                    )
                    setBuyItemPropertiesForPayflow(
                        packageName,
                        apiVersion,
                        sku,
                        type,
                        developerPayload
                    )
                    bundle = WalletUtils.startPayAsGuest(buyItemProperties)
                } else if (method is WebPayment) {
                    Log.d(
                        TAG,
                        "Service is NOT installed and should make WebFirstPayment with buyItemProperties = [$buyItemProperties]"
                    )
                    WebPaymentManager(packageName)
                        .getWebPaymentUrl(
                            BillingFlowParams(
                                sku,
                                type,
                                PayloadHelper.getOrderReference(developerPayload),
                                PayloadHelper.getPayload(developerPayload),
                                PayloadHelper.getOrigin(developerPayload)
                            )
                        )
                    bundle = WalletUtils.startWebFirstPayment()
                }
                if (bundle != null) {
                    return bundle
                }
            }
        }

        //Fallback to WalletInstallation Activity if something fails
        Log.d(
            TAG,
            "Service is NOT installed and should start install flow with buyItemProperties = [$buyItemProperties]"
        )
        setBuyItemPropertiesForPayflow(packageName, apiVersion, sku, type, developerPayload)
        return WalletUtils.startInstallFlow(buyItemProperties)
    }

    private fun setBuyItemPropertiesForPayflow(
        packageName: String,
        apiVersion: Int,
        sku: String,
        type: String,
        developerPayload: String
    ) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post {
                skuDetails = getMappedSkuDetails(sku, packageName, type)
            }
        } else {
            skuDetails = getMappedSkuDetails(sku, packageName, type)
        }

        val developerPayloadObject =
            DeveloperPayload(
                developerPayload, PayloadHelper.getPayload(developerPayload),
                PayloadHelper.getOrderReference(developerPayload),
                PayloadHelper.getOrigin(developerPayload)
            )

        buyItemProperties =
            BuyItemProperties(
                apiVersion,
                packageName,
                sku,
                type,
                developerPayloadObject,
                skuDetails
            )
    }

    override fun getPurchases(
        apiVersion: Int,
        packageName: String,
        type: String,
        continuationToken: String?
    ): Bundle {
        var bundleResponse = buildEmptyBundle()
        val walletId = walletId
        if (walletId != null && type.equals("INAPP", ignoreCase = true)) {
            val billingRepository =
                BillingRepository(BdsService(BuildConfig.HOST_WS, 30000))
            val guestPurchaseInteract =
                GuestPurchasesInteract(billingRepository)

            bundleResponse =
                guestPurchaseInteract.mapGuestPurchases(bundleResponse, walletId, packageName, type)
        }
        return bundleResponse
    }

    override fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int {
        val responseCode: Int
        val walletId = walletId
        responseCode =
            if (walletId != null && apiVersion == SUPPORTED_API_VERSION) {
                consumeGuestPurchase(walletId, packageName, purchaseToken)
            } else {
                ResponseCode.ERROR.value
            }
        return responseCode
    }

    private fun consumeGuestPurchase(
        walletId: String,
        packageName: String,
        purchaseToken: String
    ): Int {
        val billingRepository =
            BillingRepository(BdsService(BuildConfig.HOST_WS, BdsService.TIME_OUT_IN_MILLIS))
        val guestPurchaseInteract = GuestPurchasesInteract(billingRepository)

        return guestPurchaseInteract.consumeGuestPurchase(walletId, packageName, purchaseToken)
    }

    private fun buildEmptyBundle(): Bundle {
        val bundleResponse = Bundle()
        bundleResponse.putInt(Utils.RESPONSE_CODE, ResponseCode.OK.value)
        bundleResponse.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, ArrayList())
        bundleResponse.putStringArrayList(INAPP_PURCHASE_DATA_LIST, ArrayList())
        bundleResponse.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, ArrayList())
        return bundleResponse
    }

    private fun getSkuDetailsFromService(
        packageName: String,
        type: String,
        skusBundle: Bundle,
        responseWs: Bundle
    ) {
        val sku = skusBundle.getStringArrayList(Utils.GET_SKU_DETAILS_ITEM_LIST)
        val skuDetailsList = requestSkuDetails(sku, packageName, type)
        val skuDetailsResult = SkuDetailsResult(skuDetailsList, 0)
        responseWs.putInt(Utils.RESPONSE_CODE, 0)
        val skuDetails = buildResponse(skuDetailsResult)
        responseWs.putStringArrayList("DETAILS_LIST", skuDetails)
    }

    private fun getSingleSkuDetailsFromService(
        packageName: String,
        type: String,
        skusBundle: Bundle
    ): SkuDetails {
        val sku = skusBundle.getStringArrayList(Utils.GET_SKU_DETAILS_ITEM_LIST)
        return requestSingleSkuDetails(sku, packageName, type)
    }

    private fun requestSkuDetails(
        sku: List<String>?,
        packageName: String,
        type: String
    ): ArrayList<SkuDetails> {
        val skuSendList: MutableList<String> = ArrayList()
        val skuDetailsList = ArrayList<SkuDetails>()

        for (i in 1..sku!!.size) {
            skuSendList.add(sku[i - 1])
            if (i % MAX_SKUS_SEND_WS == 0 || i == sku.size) {
                val response =
                    WSServiceController.getSkuDetailsService(
                        BuildConfig.HOST_WS, packageName, skuSendList,
                        WalletUtils.getUserAgent(),
                        getPaymentFlowFromPayflowMethod(WalletUtils.getPayflowMethodsList())
                    )
                skuDetailsList.addAll(AndroidBillingMapper.mapSkuDetailsFromWS(type, response))
                skuSendList.clear()
            }
        }
        return skuDetailsList
    }

    private fun requestSingleSkuDetails(
        sku: List<String>?,
        packageName: String,
        type: String
    ): SkuDetails {
        val response =
            WSServiceController.getSkuDetailsService(
                BuildConfig.HOST_WS, packageName, sku,
                WalletUtils.getUserAgent(),
                getPaymentFlowFromPayflowMethod(WalletUtils.getPayflowMethodsList())
            )
        return AndroidBillingMapper.mapSingleSkuDetails(type, response)
    }

    private fun getMappedSkuDetails(sku: String, packageName: String, type: String): SkuDetails {
        val skuList = arrayListOf(sku)
        val skuBundle = AndroidBillingMapper.mapArrayListToBundleSkuDetails(skuList)
        return getSingleSkuDetailsFromService(packageName, type, skuBundle)
    }

    private fun buildResponse(skuDetailsResult: SkuDetailsResult): ArrayList<String> =
        ArrayList(
            skuDetailsResult.skuDetailsList.map { AndroidBillingMapper.mapSkuDetailsResponse(it) }
        )

    private fun hasRequiredFields(type: String, sku: String?): Boolean =
        type.equals("inapp", ignoreCase = true) && !sku.isNullOrEmpty()

    private val walletId: String?
        get() {
            val attributionSharedPreferences =
                AttributionSharedPreferences(WalletUtils.getContext())
            return attributionSharedPreferences.getWalletId()
        }

    private fun isTypeSupported(type: String, apiVersion: Int): Boolean =
        type.equals("inapp", ignoreCase = true) && apiVersion == SUPPORTED_API_VERSION

    companion object {
        private val TAG = WebAppcoinsBilling::class.java.simpleName

        private const val INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"
        private const val INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
        private const val INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"

        private const val SUPPORTED_API_VERSION = 3
        private const val MAX_SKUS_SEND_WS = 49 // 0 to 49

        private var skuDetails: SkuDetails? = null
        private var buyItemProperties: BuyItemProperties? = null

        private var webAppcoinsBilling: WebAppcoinsBilling? = null

        val instance: WebAppcoinsBilling?
            get() {
                if (webAppcoinsBilling == null) {
                    webAppcoinsBilling = WebAppcoinsBilling()
                }
                return webAppcoinsBilling
            }
    }
}