package com.appcoins.sdk.billing.helpers;

import static com.appcoins.sdk.billing.helpers.DeviceInformationHelperKt.getDeviceInfo;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.appcoins.billing.AppcoinsBilling;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.analytics.AnalyticsManagerProvider;
import com.appcoins.sdk.billing.analytics.IndicativeAnalytics;
import com.appcoins.sdk.billing.analytics.IndicativeLaunchCallback;
import com.appcoins.sdk.billing.analytics.SdkAnalytics;
import com.appcoins.sdk.billing.managers.ApiKeysManager;
import com.appcoins.sdk.billing.managers.WebPaymentSocketManager;
import com.appcoins.sdk.billing.payasguest.IabActivity;
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod;
import com.indicative.client.android.Indicative;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class WalletUtils {
  public static Context context;
  private static String billingPackageName;
  private static String billingIabAction;
  private static String userAgent = null;
  private static Long payAsGuestSessionId;
  private static LifecycleActivityProvider lifecycleActivityProvider;
  private static List<PaymentFlowMethod> paymentFlowMethods;
  private static String webPaymentUrl;
  private static SdkAnalytics sdkAnalytics;

  public static boolean hasBillingServiceInstalled() {
    return billingPackageName != null;
  }

  public static Bundle startServiceBind(AppcoinsBilling serviceAppcoinsBilling, int apiVersion,
      String sku, String type, String developerPayload) {
    try {
      // temporary workaround for the possibility of the endpoint failing, with two hardcoded options
      // but the logic should be reused instead of this hardcoded solution
      if (paymentFlowMethods == null) {
        if (Objects.equals(billingPackageName, BuildConfig.APPCOINS_WALLET_PACKAGE_NAME)
                || Objects.equals(billingPackageName, BuildConfig.GAMESHUB_PACKAGE_NAME)
                || Objects.equals(billingPackageName, BuildConfig.APTOIDE_GAMES_PACKAGE_NAME)) {
          return handleBindServiceAttempt(serviceAppcoinsBilling, packageToMethodName(), 1, apiVersion, sku,
              type, developerPayload);
        }
      } else {
        for (PaymentFlowMethod method : paymentFlowMethods) {
          if (method instanceof PaymentFlowMethod.Wallet
                  || method instanceof PaymentFlowMethod.GamesHub
                  || method instanceof PaymentFlowMethod.AptoideGames) {
            Bundle bundle = handleBindServiceAttempt(serviceAppcoinsBilling, method.getName(),
                method.getPriority(), apiVersion, sku, type, developerPayload);
            if (bundle != null) {
              return bundle;
            }
          }
        }
      }
      return null;
    } catch (Exception e) {
      return handleBindServiceFail(e, packageToMethodName(), 1);
    }
  }

  private static Bundle handleBindServiceAttempt(AppcoinsBilling serviceAppcoinsBilling,
      String methodName, int methodPriority, int apiVersion, String sku, String type,
      String developerPayload) {
    try {
      sdkAnalytics.sendCallBindServiceAttemptEvent(methodName, methodPriority);
      return serviceAppcoinsBilling.getBuyIntent(apiVersion, context.getPackageName(), sku, type,
          developerPayload);
    } catch (Exception e) {
      return handleBindServiceFail(e, methodName, methodPriority);
    }
  }

  private static Bundle handleBindServiceFail(Exception e, String methodName, int methodPriority) {
    sdkAnalytics.sendCallBindServiceFailEvent(methodName, methodPriority);
    e.printStackTrace();
    return null;
  }

  public static Bundle startPayAsGuest(BuyItemProperties buyItemProperties) {
    if (isMainThread()) {
      return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.getValue());
    }
    Intent intent = IabActivity.newIntent(context, buyItemProperties, sdkAnalytics);
    return createIntentBundle(intent);
  }

  public static Bundle startWebFirstPayment() {
    if (isMainThread()) {
      return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.getValue());
    }
    if (WalletUtils.getWebPaymentUrl() == null) {
      sdkAnalytics.sendWebPaymentUrlNotGeneratedEvent();
      return createBundleWithResponseCode(ResponseCode.ERROR.getValue());
    }
    int port = WebPaymentSocketManager.getInstance().startService(context);
    String paymentUrl = generatePaymentUrlWithPort(port);

    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
    return createWebIntentBundle(intent);
  }

  public static Bundle startInstallFlow(BuyItemProperties buyItemProperties) {
    if (!WalletUtils.deviceSupportsWallet(Build.VERSION.SDK_INT)) {
      return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.getValue());
    }
    Intent intent = InstallDialogActivity.newIntent(context, buyItemProperties, sdkAnalytics);
    return createIntentBundle(intent);
  }

  private static String generatePaymentUrlWithPort(int port) {
    return WalletUtils.getWebPaymentUrl() + "&wsPort=" + port;
  }

  private static Bundle createWebIntentBundle(Intent intent) {
    Bundle bundle = new Bundle();
    bundle.putParcelable("WEB_BUY_INTENT", intent);
    bundle.putInt(RESPONSE_CODE, ResponseCode.OK.getValue());
    return bundle;
  }

  private static Bundle createIntentBundle(Intent intent) {
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    Bundle bundle = new Bundle();
    bundle.putParcelable("BUY_INTENT", pendingIntent);
    bundle.putInt(RESPONSE_CODE, ResponseCode.OK.getValue());
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
        e.printStackTrace();
      }
    }
    return false;
  }

  private static Bundle createBundleWithResponseCode(int responseCode) {
    Bundle bundle = new Bundle();
    bundle.putInt(RESPONSE_CODE, responseCode);
    return bundle;
  }

  public static void setPayflowMethodsList(@Nullable List<PaymentFlowMethod> paymentFlowMethodsList) {
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
            if (isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION)
                    || isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION_ALTERNATIVE)) {
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
    billingPackageName = BuildConfig.APPCOINS_WALLET_PACKAGE_NAME;
    billingIabAction = BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION;
  }

  private static void setGamesHubBillingInfo() {
    boolean shouldUseAlternative =
        BuildConfig.DEBUG && !isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION);
    billingPackageName = shouldUseAlternative ? BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE
        : BuildConfig.GAMESHUB_PACKAGE_NAME;
    billingIabAction = shouldUseAlternative ? BuildConfig.GAMESHUB_IAB_BIND_ACTION_ALTERNATIVE
        : BuildConfig.GAMESHUB_IAB_BIND_ACTION;
  }

  private static void setAptoideGamesBillingInfo() {
      billingPackageName = BuildConfig.APTOIDE_GAMES_PACKAGE_NAME;
      billingIabAction = BuildConfig.APTOIDE_GAMES_IAB_BIND_ACTION;
  }

  private static void clearBillingServiceInfo() {
    billingPackageName = null;
    billingIabAction = null;
  }

  private static String packageToMethodName() {
    boolean shouldUseAlternative =
        BuildConfig.DEBUG && !isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION);
    String gamesHub = shouldUseAlternative ? BuildConfig.GAMESHUB_PACKAGE_NAME_ALTERNATIVE
        : BuildConfig.GAMESHUB_PACKAGE_NAME;

     if (billingPackageName == null) {
      return "unknown";
    } else {
      if (billingPackageName.equalsIgnoreCase(BuildConfig.APPCOINS_WALLET_PACKAGE_NAME)) {
          return "wallet";
      } else if (billingPackageName.equalsIgnoreCase(gamesHub)){
          return "games_hub_checkout";
      } else if (billingPackageName.equalsIgnoreCase(BuildConfig.APTOIDE_GAMES_PACKAGE_NAME)){
          return "aptoide_games"; // TODO Add correct method name.
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

  public static long getPayAsGuestSessionId() {
    if (payAsGuestSessionId == null) {
      payAsGuestSessionId = System.currentTimeMillis();
    }
    return payAsGuestSessionId;
  }

  public static void startIndicative(final String packageName) {
    launchIndicative(() -> new Thread(() -> {
      IndicativeAnalytics.INSTANCE.setInstanceId(String.valueOf(getPayAsGuestSessionId()));
      IndicativeAnalytics.INSTANCE.setIndicativeSuperProperties(packageName, BuildConfig.VERSION_CODE, getDeviceInfo());
      SdkAnalytics sdkAnalytics = new SdkAnalytics(AnalyticsManagerProvider.provideAnalyticsManager());
      sdkAnalytics.sendStartConnetionEvent();
    }).start());
  }
  private static void launchIndicative(final IndicativeLaunchCallback callback) {
    new Handler(Looper.getMainLooper()).post(() -> {
      Log.i("WalletUtils", "launchIndicative: " + ApiKeysManager.INSTANCE.getIndicativeApiKey());
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      display.getRealMetrics(displayMetrics);
    } else {
      display.getMetrics(displayMetrics);
    }
    return displayMetrics;
  }

  public static boolean deviceSupportsWallet(int sdkInt) {
    return sdkInt >= Build.VERSION_CODES.LOLLIPOP;
  }
}
