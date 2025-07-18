package com.appcoins.sdk.billing.webpayment

import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.sdk.billing.BillingFlowParams
import com.appcoins.sdk.billing.BuyItemProperties
import com.appcoins.sdk.billing.DeveloperPayload
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.SkuDetails
import com.appcoins.sdk.billing.SkuDetailsResultV2
import com.appcoins.sdk.billing.SkuDetailsV2
import com.appcoins.sdk.billing.helpers.AndroidBillingMapper
import com.appcoins.sdk.billing.helpers.PayloadHelper
import com.appcoins.sdk.billing.helpers.WalletUtils
import com.appcoins.sdk.billing.managers.ProductV2Manager
import com.appcoins.sdk.billing.mappers.PurchasesBundleMapper
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod.Companion.getPaymentFlowFromPayflowMethod
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod.WebPayment
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.DETAILS_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.GET_SKU_DETAILS_ITEM_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ITEM_LIST
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import java.io.Serializable
import java.util.concurrent.CountDownLatch

class WebAppcoinsBilling private constructor() : AppcoinsBilling, Serializable {

    init {
        webAppcoinsBillingInstance = this
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
        logInfo("Getting SKU Details.")
        val latch = CountDownLatch(1)
        val responseWs = Bundle()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Thread {
                getSkuDetailsFromService(packageName, skusBundle, responseWs)
                latch.countDown()
            }.start()
            try {
                latch.await()
            } catch (e: InterruptedException) {
                logError("Failed to get SkuDetails: $e")
                responseWs.putInt(RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.value)
            }
        } else {
            getSkuDetailsFromService(packageName, skusBundle, responseWs)
        }
        return responseWs
    }

    override fun getBuyIntent(
        apiVersion: Int,
        packageName: String,
        sku: String,
        type: String,
        developerPayload: String?,
        oemid: String?,
        guestWalletId: String?,
    ): Bundle {
        logInfo("Getting Buy Intent.")
        logDebug("BuyItemProperties = [$buyItemProperties]")
        var bundle: Bundle? = null
        if (hasRequiredFields(type, sku) && WalletUtils.paymentFlowMethods.isNotEmpty()) {
            for (method in WalletUtils.paymentFlowMethods) {
                logInfo("Payment Method ${method.name}.")
                if (method is WebPayment) {
                    logInfo("Billing App is NOT installed. Starting WebPayment.")
                    WebPaymentManager(packageName)
                        .getWebPaymentUrl(
                            BillingFlowParams(
                                sku,
                                type,
                                PayloadHelper.getOrderReference(developerPayload),
                                PayloadHelper.getPayload(developerPayload),
                                PayloadHelper.getOrigin(developerPayload),
                                PayloadHelper.getObfuscatedAccountId(developerPayload),
                                PayloadHelper.getFreeTrial(developerPayload),
                            )
                        )
                    bundle = WalletUtils.startWebFirstPayment(sku, type, method.webViewDetails)
                }
                if (bundle != null) {
                    return bundle
                }
            }
        }

        SdkAnalyticsUtils.sdkAnalytics.sendLaunchPurchaseTypeNotSupportedFailureEvent(type)

        // Fallback to WalletInstallation Activity if something fails
        logInfo("Failed to find available Payflow Method. Using fallback of install Wallet.")
        setBuyItemPropertiesForPayflow(packageName, apiVersion, sku, type, developerPayload)
        return WalletUtils.startInstallFlow(buyItemProperties)
    }

    private fun setBuyItemPropertiesForPayflow(
        packageName: String,
        apiVersion: Int,
        sku: String,
        type: String,
        developerPayload: String?
    ) {
        var skuDetails: SkuDetails? = null
        logDebug(
            "Saving Buy Item Properties:" +
                " packageName: $packageName" +
                " apiVersion: $apiVersion" +
                " sku: $sku" +
                " type: $type" +
                " developerPayload: $developerPayload"
        )
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post {
                skuDetails = getMappedSkuDetails(sku, packageName)
            }
        } else {
            skuDetails = getMappedSkuDetails(sku, packageName)
        }

        val developerPayloadObject =
            DeveloperPayload(
                developerPayload,
                PayloadHelper.getPayload(developerPayload),
                PayloadHelper.getOrderReference(developerPayload),
                PayloadHelper.getOrigin(developerPayload),
                PayloadHelper.getObfuscatedAccountId(developerPayload),
                PayloadHelper.getFreeTrial(developerPayload),
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
        logInfo("Getting Purchases of type: $type")
        var bundleResponse = buildEmptyBundle()
        val walletId = walletId
        if (walletId != null && type.equals("INAPP", ignoreCase = true)) {
            val purchasesResponse = ProductV2Manager.getPurchasesSync(packageName, walletId, type)
            bundleResponse =
                PurchasesBundleMapper().mapGuestPurchases(bundleResponse, purchasesResponse)
        } else {
            SdkAnalyticsUtils.sdkAnalytics.sendQueryPurchasesTypeNotSupportedErrorEvent(type)
            logError("Purchases type not available in WebPayments.")
        }
        return bundleResponse
    }

    override fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int {
        logInfo("Consuming Purchase.")
        logDebug("Purchase Token: $purchaseToken")
        val responseCode: Int
        val walletId = walletId
        responseCode =
            if (walletId != null && apiVersion == SUPPORTED_API_VERSION) {
                consumeGuestPurchase(walletId, packageName, purchaseToken)
            } else {
                ResponseCode.ERROR.value
            }
        logInfo("Result of Consume: $responseCode")
        return responseCode
    }

    private fun consumeGuestPurchase(
        walletId: String,
        packageName: String,
        purchaseToken: String
    ): Int = ProductV2Manager.consumePurchase(walletId, packageName, purchaseToken)

    private fun buildEmptyBundle(): Bundle {
        val bundleResponse = Bundle()
        bundleResponse.putInt(RESPONSE_CODE, ResponseCode.OK.value)
        bundleResponse.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, ArrayList())
        bundleResponse.putStringArrayList(INAPP_PURCHASE_DATA_LIST, ArrayList())
        bundleResponse.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, ArrayList())
        return bundleResponse
    }

    private fun getSkuDetailsFromService(
        packageName: String,
        skusBundle: Bundle,
        responseWs: Bundle
    ) {
        val sku = skusBundle.getStringArrayList(GET_SKU_DETAILS_ITEM_LIST)
        val skuDetailsList = sku?.let { requestSkuDetails(sku, packageName) } ?: emptyList()
        val skuDetailsResult = SkuDetailsResultV2(skuDetailsList, 0)
        responseWs.putInt(RESPONSE_CODE, 0)
        val skuDetails = buildResponse(skuDetailsResult)
        responseWs.putStringArrayList(DETAILS_LIST, skuDetails)
    }

    private fun getSingleSkuDetailsFromService(packageName: String, skusBundle: Bundle): SkuDetails? {
        val sku = skusBundle.getStringArrayList(GET_SKU_DETAILS_ITEM_LIST)
        return requestSingleSkuDetails(sku, packageName)
    }

    private fun requestSkuDetails(sku: List<String>, packageName: String): ArrayList<SkuDetailsV2> {
        val skuDetailsList = ArrayList<SkuDetailsV2>()
        val skuSendList: ArrayList<String> = ArrayList()

        for (i in 1..sku.size) {
            skuSendList.add(sku[i - 1])
            if (i % MAX_SKUS_SEND_WS == 0 || i == sku.size) {
                val skuDetailsResponse = ProductV2Manager.getSkuDetails(
                    packageName,
                    skuSendList,
                    getPaymentFlowFromPayflowMethod(WalletUtils.paymentFlowMethods.toMutableList())
                )
                skuDetailsList.addAll(skuDetailsResponse?.items ?: emptyList())
                skuSendList.clear()
            }
        }
        return skuDetailsList
    }

    private fun requestSingleSkuDetails(
        sku: List<String>?,
        packageName: String
    ): SkuDetails? {
        if (sku.isNullOrEmpty()) {
            return null
        }

        val skuDetailsResponse =
            ProductV2Manager.getSkuDetails(
                packageName,
                arrayListOf(sku.first()),
                getPaymentFlowFromPayflowMethod(WalletUtils.paymentFlowMethods.toMutableList())
            )

        return skuDetailsResponse?.items?.firstOrNull()?.toSkuDetails()
    }

    private fun getMappedSkuDetails(sku: String, packageName: String): SkuDetails? {
        val skuList = arrayListOf(sku)
        val skuBundle = AndroidBillingMapper.mapArrayListToBundleSkuDetails(skuList)
        return getSingleSkuDetailsFromService(packageName, skuBundle)
    }

    private fun buildResponse(skuDetailsResultV2: SkuDetailsResultV2): ArrayList<String> =
        ArrayList(
            skuDetailsResultV2.skuDetailsList.map { it.toSkuDetailsResponseString() }
        )

    private fun hasRequiredFields(type: String, sku: String?): Boolean =
        type.equals("inapp", ignoreCase = true) && !sku.isNullOrEmpty()

    private val walletId: String?
        get() {
            val attributionSharedPreferences = AttributionSharedPreferences(WalletUtils.context)
            return attributionSharedPreferences.getWalletId()
        }

    private fun isTypeSupported(type: String, apiVersion: Int): Boolean =
        type.equals("inapp", ignoreCase = true) && apiVersion == SUPPORTED_API_VERSION

    companion object {
        private const val SUPPORTED_API_VERSION = 3
        private const val MAX_SKUS_SEND_WS = 49 // 0 to 49

        private var buyItemProperties: BuyItemProperties? = null

        private var webAppcoinsBillingInstance: WebAppcoinsBilling? = null

        val instance: WebAppcoinsBilling?
            get() {
                if (webAppcoinsBillingInstance == null) {
                    webAppcoinsBillingInstance = WebAppcoinsBilling()
                }
                return webAppcoinsBillingInstance
            }
    }
}
