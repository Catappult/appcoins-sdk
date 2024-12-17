package com.appcoins.sdk.billing.helpers

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import com.appcoins.billing.sdk.BuildConfig
import com.appcoins.sdk.billing.BuyItemProperties
import com.appcoins.sdk.billing.ResponseCode
import com.appcoins.sdk.billing.activities.BillingFlowActivity.Companion.newIntent
import com.appcoins.sdk.billing.activities.InstallDialogActivity
import com.appcoins.sdk.billing.analytics.AnalyticsManagerProvider
import com.appcoins.sdk.billing.analytics.IndicativeAnalytics.instanceId
import com.appcoins.sdk.billing.analytics.IndicativeAnalytics.setIndicativeSuperProperties
import com.appcoins.sdk.billing.analytics.SdkAnalytics
import com.appcoins.sdk.billing.managers.ApiKeysManager.getIndicativeApiKey
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.AptoideGames
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.GamesHub
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.Wallet
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod.WebPayment.WebViewDetails
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.KEY_BUY_INTENT
import com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE
import com.appcoins.sdk.billing.webpayment.WebPaymentActivity.Companion.newIntent
import com.appcoins.sdk.core.logger.Logger.logDebug
import com.appcoins.sdk.core.logger.Logger.logError
import com.appcoins.sdk.core.logger.Logger.logInfo
import com.appcoins.sdk.core.logger.Logger.logWarning
import com.indicative.client.android.Indicative
import java.util.concurrent.CountDownLatch

@Suppress("StaticFieldLeak", "TooManyFunctions")
object WalletUtils {
    var billingServicePackageName: String? = null
    var billingServiceIabAction: String? = null
    var paymentFlowMethods: List<PaymentFlowMethod> = emptyList()
        set(paymentFlowMethods) {
            field = paymentFlowMethods
            setBillingServiceInfoToBind()
        }
    var webPaymentUrl: String? = null
    lateinit var context: Context

    val sdkAnalytics: SdkAnalytics by lazy { SdkAnalytics(AnalyticsManagerProvider.provideAnalyticsManager()) }
    val userAgent: String by lazy {
        val displayMetrics = getDisplayMetrics()
        val widthPixels = displayMetrics.widthPixels
        val heightPixels = displayMetrics.heightPixels
        buildUserAgent(widthPixels, heightPixels)
    }

    fun hasBillingServiceInstalled(): Boolean =
        billingServicePackageName != null

    fun startWebFirstPayment(sku: String, skuType: String, webViewDetails: WebViewDetails?): Bundle {
        logInfo("Creating WebPayment bundle.")
        if (isMainThread()) {
            logError("WebPayment is not available in MainThread.")
            return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.value)
        }
        if (webPaymentUrl == null) {
            logError("Failure obtaining WebPayment URL.")
            sdkAnalytics.sendWebPaymentUrlNotGeneratedEvent()
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

    fun startIndicative(packageName: String?) {
        logInfo("Starting Indicative for $packageName")
        launchIndicative {
            Thread {
                val walletId = getWalletIdForUserSession()
                logDebug(
                    "Parameters for indicative: walletId: $walletId" +
                        " packageName: $packageName" +
                        " versionCode: ${BuildConfig.VERSION_CODE}"
                )
                instanceId = walletId
                setIndicativeSuperProperties(packageName, BuildConfig.VERSION_CODE, getDeviceInfo())
                sdkAnalytics.sendStartConnectionEvent()
            }.start()
        }
    }

    private fun launchIndicative(callback: () -> Unit) =
        Handler(Looper.getMainLooper()).post {
            Indicative.launch(context, getIndicativeApiKey())
            callback()
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

    @Suppress("NestedBlockDepth")
    private fun setBillingServiceInfoToBind() {
        clearBillingServiceInfo()
        if (paymentFlowMethods.isEmpty()) {
            setDefaultBillingServiceInfoToBind()
        } else {
            for (method in paymentFlowMethods) {
                when (method) {
                    is Wallet ->
                        if (isAppAvailableToBind(BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION)) {
                            setWalletBillingInfo()
                        }

                    is GamesHub ->
                        if (isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION) ||
                            isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION_ALTERNATIVE)
                        ) {
                            setGamesHubBillingInfo()
                        }

                    is AptoideGames ->
                        if (isAppAvailableToBind(BuildConfig.APTOIDE_GAMES_IAB_BIND_ACTION)) {
                            setAptoideGamesBillingInfo()
                        }

                    else -> clearBillingServiceInfo()
                }
                if (billingServicePackageName != null) {
                    break
                }
            }
        }
    }

    private fun setDefaultBillingServiceInfoToBind() =
        when {
            isAppAvailableToBind(BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION) -> setWalletBillingInfo()
            isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION) -> setGamesHubBillingInfo()
            isAppAvailableToBind(BuildConfig.APTOIDE_GAMES_IAB_BIND_ACTION) -> setAptoideGamesBillingInfo()
            else -> clearBillingServiceInfo()
        }

    private fun setWalletBillingInfo() {
        logInfo("Setting Wallet Billing info.")
        billingServicePackageName = BuildConfig.APPCOINS_WALLET_PACKAGE_NAME
        billingServiceIabAction = BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION
    }

    private fun setGamesHubBillingInfo() {
        logInfo("Setting GamesHub Billing info.")
        val shouldUseAlternative = BuildConfig.DEBUG && !isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION)
        billingServicePackageName =
            if (shouldUseAlternative) {
                BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE
            } else {
                BuildConfig.GAMESHUB_PACKAGE_NAME
            }
        billingServiceIabAction =
            if (shouldUseAlternative) {
                BuildConfig.GAMESHUB_IAB_BIND_ACTION_ALTERNATIVE
            } else {
                BuildConfig.GAMESHUB_IAB_BIND_ACTION
            }
    }

    private fun setAptoideGamesBillingInfo() {
        logInfo("Setting AptoideGames Billing info.")
        billingServicePackageName = BuildConfig.APTOIDE_GAMES_PACKAGE_NAME
        billingServiceIabAction = BuildConfig.APTOIDE_GAMES_IAB_BIND_ACTION
    }

    private fun clearBillingServiceInfo() {
        logInfo("Clearing Billing info.")
        billingServicePackageName = null
        billingServiceIabAction = null
    }

    private fun isAppAvailableToBind(action: String?): Boolean {
        val intent = Intent(action)
        val resolveInfoList = context.packageManager.queryIntentServices(intent, 0)
        return resolveInfoList.isNotEmpty()
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
