package com.appcoins.sdk.billing.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.MATCH_ALL
import android.content.pm.PackageManager.ResolveInfoFlags
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.communication.requester.MessageRequesterFactory
import com.appcoins.sdk.billing.BuyItemProperties
import com.appcoins.sdk.billing.FeatureType
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.UriCommunicationAppcoinsBilling
import com.appcoins.sdk.billing.activities.BillingFlowActivity.Companion.newIntent
import com.appcoins.sdk.billing.activities.InstallDialogActivity
import com.appcoins.sdk.billing.activities.UnavailableBillingDialogActivity
import com.appcoins.sdk.billing.managers.ApiKeysManager.getIndicativeApiKey
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod.AptoideGames
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod.GamesHub
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod.Wallet
import com.appcoins.sdk.billing.payflow.models.WebViewDetails
import com.appcoins.sdk.billing.service.BdsService
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.types.SkuType
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.KEY_BUY_INTENT
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.appcoins.sdk.billing.webpayment.WebPaymentActivity.Companion.newIntent
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils
import com.appcoins.sdk.core.analytics.indicative.IndicativeAnalytics.setupIndicativeProperties
import com.appcoins.sdk.core.device.getDeviceInfo
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.logger.Logger.logWarning
import com.indicative.client.android.Indicative
import java.util.concurrent.CountDownLatch

@Suppress("StaticFieldLeak", "TooManyFunctions")
object WalletUtils {
    var paymentFlowMethods: List<PaymentFlowMethod> = emptyList()
    var currentPaymentFlowMethod: PaymentFlowMethod? = null
    val localPaymentFlowMethods =
        listOf(
            Wallet("wallet", 1, arrayListOf(FeatureType.SUBSCRIPTIONS)),
            GamesHub("games_hub_checkout", 2, arrayListOf()),
            AptoideGames("aptoide_games", 3, arrayListOf())
        )
    var webPaymentUrl: String? = null
    lateinit var context: Context

    val userAgent: String by lazy {
        val displayMetrics = getDisplayMetrics()
        val widthPixels = displayMetrics.widthPixels
        val heightPixels = displayMetrics.heightPixels
        buildUserAgent(widthPixels, heightPixels)
    }

    fun startWebFirstPayment(sku: String, skuType: String, webViewDetails: WebViewDetails?): Bundle {
        logInfo("Creating WebPayment bundle.")
        if (isMainThread()) {
            logError("WebPayment is not available in MainThread.")
            return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.value)
        }
        if (webPaymentUrl == null) {
            logError("Failure obtaining WebPayment URL.")
            SdkAnalyticsUtils.sdkAnalytics.sendWebPaymentFailureToObtainUrlEvent()
            return createBundleWithResponseCode(ResponseCode.ERROR.value)
        }

        val intent = newIntent(context, webPaymentUrl!!, sku, skuType, webViewDetails)
        val intentBundle = createIntentBundle(intent, ResponseCode.OK.value)
        logDebug("WebPayment intentBundle:$intentBundle")
        return intentBundle
    }

    fun startWalletPayment(bundle: Bundle, skuType: String): Bundle {
        logInfo("Creating Wallet bundle.")
        val intent = newIntent(context, bundle, skuType)
        val intentBundle = createIntentBundle(intent, bundle.getInt(RESPONSE_CODE))
        logDebug("WalletPayment intentBundle:$intentBundle")
        return intentBundle
    }

    fun startInstallFlow(buyItemProperties: BuyItemProperties?): Bundle {
        logInfo("Creating InstallWallet bundle.")
        if (!deviceSupportsWallet()) {
            logError("Wallet NOT Supported in this version: " + Build.VERSION.SDK_INT)
            return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.value)
        }
        val intent = InstallDialogActivity.newIntent(context, buyItemProperties)
        val intentBundle = createIntentBundle(intent, ResponseCode.OK.value)
        logDebug("InstallWallet intentBundle:$intentBundle")
        return intentBundle
    }

    fun startServiceUnavailableDialog(message: String?): Bundle {
        logInfo("Creating BillingUnavailableDialog bundle.")
        val intent = UnavailableBillingDialogActivity.newIntent(context, message)
        val intentBundle = createIntentBundle(intent, ResponseCode.OK.value)
        return intentBundle
    }

    fun startIndicative(packageName: String?) {
        logInfo("Starting Indicative for $packageName")
        if (!SdkAnalyticsUtils.isIndicativeEventLoggerInitialized) {
            launchIndicative {
                SdkAnalyticsUtils.isIndicativeEventLoggerInitialized = true
                val walletId = getWalletIdForUserSession()
                logDebug(
                    "Parameters for indicative: walletId: $walletId" +
                        " packageName: $packageName" +
                        " versionCode: ${BuildConfig.VERSION_CODE}"
                )

                setupIndicativeProperties(packageName, BuildConfig.VERSION_CODE, getDeviceInfo(), walletId)
                SdkAnalyticsUtils.sdkAnalytics.sendStartConnectionEvent()
            }
        }
    }

    fun getBillingPackageNameFromPaymentFlowMethod(paymentFlowMethod: PaymentFlowMethod): String? =
        when (paymentFlowMethod) {
            is Wallet -> BuildConfig.APPCOINS_WALLET_PACKAGE_NAME
            is GamesHub -> BuildConfig.GAMESHUB_PACKAGE_NAME
            is AptoideGames -> BuildConfig.APTOIDE_GAMES_PACKAGE_NAME
            else -> null
        }

    fun getBillingIabActionNameFromPaymentFlowMethod(paymentFlowMethod: PaymentFlowMethod): String? =
        when (paymentFlowMethod) {
            is Wallet -> BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION
            is GamesHub -> BuildConfig.GAMESHUB_IAB_BIND_ACTION
            is AptoideGames -> BuildConfig.APTOIDE_GAMES_IAB_BIND_ACTION
            else -> null
        }

    fun isAppAvailableToBind(action: String?): Boolean {
        val intent = Intent(action)
        val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentServices(intent, ResolveInfoFlags.of(MATCH_ALL.toLong()))
        } else {
            context.packageManager.queryIntentServices(intent, 0)
        }
        logInfo("Resolve Information list contains ${resolveInfoList.size} packages for action $action.")
        resolveInfoList.forEach {
            logInfo("Found following packages to bind: $it")
        }
        return resolveInfoList.isNotEmpty()
    }

    fun isUriBillingSupported(): Boolean {
        if (!BuildConfig.URI_COMMUNICATION) {
            return false
        }
        val messageRequester =
            MessageRequesterFactory.create(
                context,
                BuildConfig.APPCOINS_WALLET_PACKAGE_NAME,
                "appcoins://billing/communication/processor/1",
                "appcoins://billing/communication/requester/1",
                BdsService.TIME_OUT_IN_MILLIS
            )
        val uriCommunicationAppcoinsBilling = UriCommunicationAppcoinsBilling(messageRequester)
        return try {
            val result = uriCommunicationAppcoinsBilling.isBillingSupported(3, context.packageName, SkuType.inapp.name)
            result == ResponseCode.OK.value
        } catch (ex: Exception) {
            logError("Failed to verify if URI Communication Protocol is available.", ex)
            false
        }
    }

    private fun launchIndicative(callback: () -> Unit) {
        try {
            Indicative.launch(context, getIndicativeApiKey())
        } catch (ex: Exception) {
            logError("Failed to Launch Indicative.", ex)
        } finally {
            callback()
        }
    }

    private fun isMainThread(): Boolean {
        val latch = CountDownLatch(1)
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Thread { latch.countDown() }.start()
            try {
                latch.await()
                return true
            } catch (e: InterruptedException) {
                logWarning("Timeout verifying MainThread: $e")
            }
        }
        return false
    }

    private fun getWalletIdForUserSession(): String {
        val walletId = AttributionSharedPreferences(context).getWalletId()
        return walletId ?: System.currentTimeMillis().toString()
    }

    private fun getDisplayMetrics(): DisplayMetrics {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getRealMetrics(displayMetrics)
        return displayMetrics
    }

    private fun createIntentBundle(intent: Intent, responseCode: Int): Bundle =
        Bundle().apply {
            putParcelable(KEY_BUY_INTENT, intent)
            putInt(RESPONSE_CODE, responseCode)
        }

    private fun createBundleWithResponseCode(responseCode: Int): Bundle =
        Bundle().apply {
            putInt(RESPONSE_CODE, responseCode)
        }

    private fun buildUserAgent(widthPixels: Int, heightPixels: Int): String {
        return "AppCoinsGuestSDK/${BuildConfig.VERSION_NAME} (Linux; Android ${
            Build.VERSION.RELEASE.replace(
                ";".toRegex(),
                " "
            )
        }; ${Build.VERSION.SDK_INT}; ${Build.MODEL.replace(";".toRegex(), " ")} Build/${
            Build.PRODUCT.replace(
                ";",
                " "
            )
        }; ${System.getProperty("os.arch")}; " +
            "${context.packageName}; ${BuildConfig.VERSION_CODE}; ${widthPixels}x$heightPixels)"
    }

    private fun deviceSupportsWallet(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
}
