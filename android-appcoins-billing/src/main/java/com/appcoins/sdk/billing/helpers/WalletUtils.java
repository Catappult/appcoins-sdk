package com.appcoins.sdk.billing.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.appcoins.billing.AppcoinsBilling;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.activities.BillingFlowActivity;
import com.appcoins.sdk.billing.analytics.AnalyticsManagerProvider;
import com.appcoins.sdk.billing.analytics.IndicativeAnalytics;
import com.appcoins.sdk.billing.analytics.IndicativeLaunchCallback;
import com.appcoins.sdk.billing.analytics.SdkAnalytics;
import com.appcoins.sdk.billing.managers.ApiKeysManager;
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod;
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences;
import com.appcoins.sdk.billing.webpayment.WebPaymentActivity;
import com.indicative.client.android.Indicative;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.jetbrains.annotations.Nullable;

import static com.appcoins.sdk.billing.helpers.DeviceInformationHelperKt.getDeviceInfo;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.KEY_BUY_INTENT;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE;
import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logError;
import static com.appcoins.sdk.core.logger.Logger.logInfo;
import static com.appcoins.sdk.core.logger.Logger.logWarning;

public class WalletUtils {
    public static Context context;
    private static String billingPackageName;
    private static String billingIabAction;
    private static String userAgent = null;
    private static LifecycleActivityProvider lifecycleActivityProvider;
    private static List<PaymentFlowMethod> paymentFlowMethods;
    private static String webPaymentUrl;
    private static SdkAnalytics sdkAnalytics;

    public static boolean hasBillingServiceInstalled() {
        return billingPackageName != null;
    }

    public static Bundle startServiceBind(AppcoinsBilling serviceAppcoinsBilling, int apiVersion, String sku,
        String type, String developerPayload, String oemid, String guestWalletId) {
        try {
            // temporary workaround for the possibility of the endpoint failing, with two
            // hardcoded options
            // but the logic should be reused instead of this hardcoded solution
            if (paymentFlowMethods == null) {
                logInfo("PaymentFlowMethods is null");
                if (Objects.equals(billingPackageName, BuildConfig.APPCOINS_WALLET_PACKAGE_NAME) || Objects.equals(
                    billingPackageName, BuildConfig.GAMESHUB_PACKAGE_NAME) || Objects.equals(billingPackageName,
                    BuildConfig.APTOIDE_GAMES_PACKAGE_NAME)) {
                    logInfo("billingPackageName: " + billingPackageName);
                    return handleBindServiceAttempt(serviceAppcoinsBilling, packageToMethodName(), 1, apiVersion, sku,
                        type, developerPayload, oemid, guestWalletId);
                }
            } else {
                for (PaymentFlowMethod method : paymentFlowMethods) {
                    if (method instanceof PaymentFlowMethod.Wallet
                        || method instanceof PaymentFlowMethod.GamesHub
                        || method instanceof PaymentFlowMethod.AptoideGames) {
                        logInfo("PaymentFlowMethod found: " + method.getName());
                        Bundle bundle =
                            handleBindServiceAttempt(serviceAppcoinsBilling, method.getName(), method.getPriority(),
                                apiVersion, sku, type, developerPayload, oemid, guestWalletId);
                        if (bundle != null) {
                            return bundle;
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            logError("Failure getting BuyIntent from any Billing Service.", e);
            return handleBindServiceFail(packageToMethodName(), 1);
        }
    }

    @SuppressWarnings({ "ant:checkstyle:ParameterNumber", "ant:checkstyle:ParameterNumber", })
    private static Bundle handleBindServiceAttempt(AppcoinsBilling serviceAppcoinsBilling, String methodName,
        int methodPriority, int apiVersion, String sku, String type, String developerPayload, String oemid,
        String guestWalletId) {
        try {
            logInfo("Getting BuyIntent from BillingApp.");
            sdkAnalytics.sendCallBindServiceAttemptEvent(methodName, methodPriority);
            return serviceAppcoinsBilling.getBuyIntent(apiVersion, context.getPackageName(), sku, type,
                developerPayload, oemid, guestWalletId);
        } catch (Exception e) {
            logError("Failure getting BuyIntent from BillingApp.", e);
            return handleBindServiceFail(methodName, methodPriority);
        }
    }

    private static Bundle handleBindServiceFail(String methodName, int methodPriority) {
        sdkAnalytics.sendCallBindServiceFailEvent(methodName, methodPriority);
        return null;
    }

    public static Bundle startWebFirstPayment(String sku, String paymentFlow) {
        logInfo("Creating WebPayment bundle.");
        if (isMainThread()) {
            logError("WebPayment is not available in MainThread.");
            return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.getValue());
        }
        if (WalletUtils.getWebPaymentUrl() == null) {
            logError("Failure obtaining WebPayment URL.");
            sdkAnalytics.sendWebPaymentUrlNotGeneratedEvent();
            return createBundleWithResponseCode(ResponseCode.ERROR.getValue());
        }

        Intent intent = WebPaymentActivity.newIntent(context, WalletUtils.getWebPaymentUrl(), sku, paymentFlow);
        Bundle intentBundle = createIntentBundle(intent, ResponseCode.OK.getValue());
        logDebug("WebPayment intentBundle:" + intentBundle);
        return intentBundle;
    }

    public static Bundle startWalletPayment(Bundle bundle) {
        logInfo("Creating Wallet bundle.");
        Intent intent = BillingFlowActivity.newIntent(context, bundle);
        Bundle intentBundle = createIntentBundle(intent, bundle.getInt(RESPONSE_CODE));
        logDebug("WalletPayment intentBundle:" + intentBundle);
        return intentBundle;
    }

    public static Bundle startInstallFlow(BuyItemProperties buyItemProperties) {
        logInfo("Creating InstallWallet bundle.");
        if (!WalletUtils.deviceSupportsWallet(Build.VERSION.SDK_INT)) {
            logError("Wallet NOT Supported in this version: " + Build.VERSION.SDK_INT);
            return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.getValue());
        }
        Intent intent = InstallDialogActivity.newIntent(context, buyItemProperties, sdkAnalytics);
        Bundle intentBundle = createIntentBundle(intent, ResponseCode.OK.getValue());
        logDebug("InstallWallet intentBundle:" + intentBundle);
        return intentBundle;
    }

    private static Bundle createIntentBundle(Intent intent, Integer responseCode) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_BUY_INTENT, intent);
        bundle.putInt(RESPONSE_CODE, responseCode);
        return bundle;
    }

    private static boolean isMainThread() {
        final CountDownLatch latch = new CountDownLatch(1);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(latch::countDown).start();
            try {
                latch.await();
                return true;
            } catch (InterruptedException e) {
                logWarning("Timeout verifying MainThread: " + e);
            }
        }
        return false;
    }

    private static Bundle createBundleWithResponseCode(int responseCode) {
        Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, responseCode);
        return bundle;
    }

    public static void setPayflowMethodsList(
        @Nullable
        List<PaymentFlowMethod> paymentFlowMethodsList) {
        paymentFlowMethods = paymentFlowMethodsList;
        setBillingServiceInfoToBind();
    }

    public static List<PaymentFlowMethod> getPayflowMethodsList() {
        if (paymentFlowMethods == null || paymentFlowMethods.isEmpty()) {
            return Collections.emptyList();
        } else {
            return paymentFlowMethods;
        }
    }

    public static void setWebPaymentUrl(String webPaymentUrlToSet) {
        webPaymentUrl = webPaymentUrlToSet;
    }

    public static String getWebPaymentUrl() {
        return webPaymentUrl;
    }

    public static String getBillingServicePackageName() {
        return billingPackageName;
    }

    public static String getBillingServiceIabAction() {
        return billingIabAction;
    }

    public static void setBillingServiceInfoToBind() {
        clearBillingServiceInfo();
        if (paymentFlowMethods == null) {
            setDefaultBillingServiceInfoToBind();
        } else if (paymentFlowMethods.isEmpty()) {
            clearBillingServiceInfo();
        } else {
            for (PaymentFlowMethod method : paymentFlowMethods) {
                if (method instanceof PaymentFlowMethod.Wallet) {
                    if (isAppAvailableToBind(BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION)) {
                        setWalletBillingInfo();
                    }
                } else if (method instanceof PaymentFlowMethod.GamesHub) {
                    if (isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION) || isAppAvailableToBind(
                        BuildConfig.GAMESHUB_IAB_BIND_ACTION_ALTERNATIVE)) {
                        setGamesHubBillingInfo();
                    }
                } else if (method instanceof PaymentFlowMethod.AptoideGames) {
                    if (isAppAvailableToBind(BuildConfig.APTOIDE_GAMES_IAB_BIND_ACTION)) {
                        setAptoideGamesBillingInfo();
                    }
                } else {
                    clearBillingServiceInfo();
                }
                if (billingPackageName != null) {
                    break;
                }
            }
        }
    }

    private static void setDefaultBillingServiceInfoToBind() {
        if (isAppAvailableToBind(BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION)) {
            setWalletBillingInfo();
        } else if (isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION)) {
            setGamesHubBillingInfo();
        } else if (isAppAvailableToBind(BuildConfig.APTOIDE_GAMES_IAB_BIND_ACTION)) {
            setAptoideGamesBillingInfo();
        } else {
            clearBillingServiceInfo();
        }
    }

    private static void setWalletBillingInfo() {
        logInfo("Setting Wallet Billing info.");
        billingPackageName = BuildConfig.APPCOINS_WALLET_PACKAGE_NAME;
        billingIabAction = BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION;
    }

    private static void setGamesHubBillingInfo() {
        logInfo("Setting GamesHub Billing info.");
        boolean shouldUseAlternative = BuildConfig.DEBUG && !isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION);
        billingPackageName =
            shouldUseAlternative ? BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE : BuildConfig.GAMESHUB_PACKAGE_NAME;
        billingIabAction = shouldUseAlternative ? BuildConfig.GAMESHUB_IAB_BIND_ACTION_ALTERNATIVE
            : BuildConfig.GAMESHUB_IAB_BIND_ACTION;
    }

    private static void setAptoideGamesBillingInfo() {
        logInfo("Setting AptoideGames Billing info.");
        billingPackageName = BuildConfig.APTOIDE_GAMES_PACKAGE_NAME;
        billingIabAction = BuildConfig.APTOIDE_GAMES_IAB_BIND_ACTION;
    }

    private static void clearBillingServiceInfo() {
        logInfo("Clearing Billing info.");
        billingPackageName = null;
        billingIabAction = null;
    }

    private static String packageToMethodName() {
        boolean shouldUseAlternative = BuildConfig.DEBUG && !isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION);
        String gamesHub =
            shouldUseAlternative ? BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE : BuildConfig.GAMESHUB_PACKAGE_NAME;

        if (billingPackageName == null) {
            return "unknown";
        } else {
            if (billingPackageName.equalsIgnoreCase(BuildConfig.APPCOINS_WALLET_PACKAGE_NAME)) {
                return "wallet";
            } else if (billingPackageName.equalsIgnoreCase(gamesHub)) {
                return "games_hub_checkout";
            } else if (billingPackageName.equalsIgnoreCase(BuildConfig.APTOIDE_GAMES_PACKAGE_NAME)) {
                return "aptoide_games";
            } else {
                return "unknown";
            }
        }
    }

    public static boolean isAppAvailableToBind(String action) {
        Intent intent = new Intent(action);
        List<ResolveInfo> resolveInfoList = context.getPackageManager()
            .queryIntentServices(intent, 0);
        return !resolveInfoList.isEmpty();
    }

    public static void initIap(Context context) {
        Context applicationContext = context.getApplicationContext();
        WalletUtils.context = applicationContext;
        if (lifecycleActivityProvider == null) {
            lifecycleActivityProvider = new LifecycleActivityProvider(applicationContext);
        }
    }

    public static LifecycleActivityProvider getLifecycleActivityProvider() {
        return lifecycleActivityProvider;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        WalletUtils.context = context;
        initIap(context);
    }

    public static String getWalletIdForUserSession() {
        String walletId = new AttributionSharedPreferences(context).getWalletId();
        return walletId != null ? walletId : String.valueOf(System.currentTimeMillis());
    }

    public static void startIndicative(final String packageName) {
        logInfo(String.format("Starting Indicative for %s", packageName));
        launchIndicative(() -> new Thread(() -> {
            String walletId = getWalletIdForUserSession();
            logDebug(
                String.format("Parameters for indicative:" + " walletId: %s" + " packageName: %s" + " versionCode: %s",
                    walletId, packageName, BuildConfig.VERSION_CODE));
            IndicativeAnalytics.INSTANCE.setInstanceId(walletId);
            IndicativeAnalytics.INSTANCE.setIndicativeSuperProperties(packageName, BuildConfig.VERSION_CODE,
                getDeviceInfo());
            SdkAnalytics sdkAnalytics = new SdkAnalytics(AnalyticsManagerProvider.provideAnalyticsManager());
            sdkAnalytics.sendStartConnetionEvent();
        }).start());
    }

    private static void launchIndicative(final IndicativeLaunchCallback callback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Indicative.launch(context, ApiKeysManager.INSTANCE.getIndicativeApiKey());
            if (callback != null) {
                callback.onLaunchComplete();
            }
        });
    }

    public static SdkAnalytics getSdkAnalytics() {
        if (sdkAnalytics == null) {
            sdkAnalytics = new SdkAnalytics(AnalyticsManagerProvider.provideAnalyticsManager());
        }
        return sdkAnalytics;
    }

    public static String getUserAgent() {
        if (userAgent == null) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            int widthPixels = 0;
            int heightPixels = 0;
            if (displayMetrics != null) {
                widthPixels = displayMetrics.widthPixels;
                heightPixels = displayMetrics.heightPixels;
            }
            userAgent = buildUserAgent(widthPixels, heightPixels);
        }
        return userAgent;
    }

    private static String buildUserAgent(int widthPixels, int heightPixels) {
        return "AppCoinsGuestSDK/"
            + BuildConfig.VERSION_NAME
            + " (Linux; Android "
            + Build.VERSION.RELEASE.replaceAll(";", " ")
            + "; "
            + Build.VERSION.SDK_INT
            + "; "
            + Build.MODEL.replaceAll(";", " ")
            + " Build/"
            + Build.PRODUCT.replace(";", " ")
            + "; "
            + System.getProperty("os.arch")
            + "; "
            + context.getPackageName()
            + "; "
            + BuildConfig.VERSION_CODE
            + "; "
            + widthPixels
            + "x"
            + heightPixels
            + ")";
    }

    private static DisplayMetrics getDisplayMetrics() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return null;
        }
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    public static boolean deviceSupportsWallet(int sdkInt) {
        return sdkInt >= Build.VERSION_CODES.LOLLIPOP;
    }
}
