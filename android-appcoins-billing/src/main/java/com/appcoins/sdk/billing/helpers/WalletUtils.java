package com.appcoins.sdk.billing.helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import com.appcoins.sdk.billing.analytics.SdkAnalytics;
import com.appcoins.sdk.billing.payasguest.IabActivity;
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod;
import com.indicative.client.android.Indicative;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.appcoins.sdk.billing.helpers.DeviceInformationHelperKt.getDeviceInfo;

public class WalletUtils {
  public static Context context;
  private static String billingPackageName;
  private static String billingIabAction;
  private static String userAgent = null;
  private static Long payAsGuestSessionId;
  private static LifecycleActivityProvider lifecycleActivityProvider;
  private static List<PaymentFlowMethod> paymentFlowMethods;
  private static SdkAnalytics sdkAnalytics;

  public static boolean hasBillingServiceInstalled() {
    if (billingPackageName == null) {
      setBillingServiceInfoToBind();
    }
    return billingPackageName != null;
  }

  public static Bundle startServiceBind(AppcoinsBilling serviceAppcoinsBilling, int apiVersion,
      String sku, String type, String developerPayload) {
    try {
      if ((paymentFlowMethods == null || paymentFlowMethods.isEmpty()) && isAppAvailableToBind(BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION)) {
        return handleBindServiceAttempt(serviceAppcoinsBilling, "wallet", 1, apiVersion, sku, type, developerPayload);
      } else {
        for (PaymentFlowMethod method : paymentFlowMethods) {
          if (method instanceof PaymentFlowMethod.Wallet || method instanceof PaymentFlowMethod.GamesHub) {
            Bundle bundle = handleBindServiceAttempt(serviceAppcoinsBilling, method.getName(), method.getPriority(),
                apiVersion, sku, type, developerPayload);
            if (bundle != null) {
              return bundle;
            }
          }
        }
      }
      return null;
    } catch (Exception e) {
      return handleBindServiceFail(e, "wallet", 1);
    }
  }

  private static Bundle handleBindServiceAttempt(AppcoinsBilling serviceAppcoinsBilling, String methodName,
      int methodPriority, int apiVersion, String sku,
      String type, String developerPayload) {
    try {
      sdkAnalytics.sendCallBindServiceAttemptEvent(methodName, methodPriority);
      return serviceAppcoinsBilling.getBuyIntent(apiVersion, context.getPackageName(), sku, type, developerPayload);
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

  public static Bundle startInstallFlow(BuyItemProperties buyItemProperties) {
    if (!WalletUtils.deviceSupportsWallet(Build.VERSION.SDK_INT)) {
      return createBundleWithResponseCode(ResponseCode.BILLING_UNAVAILABLE.getValue());
    }
    Intent intent = InstallDialogActivity.newIntent(context, buyItemProperties, sdkAnalytics);
    return createIntentBundle(intent);
  }

  private static Bundle createIntentBundle(Intent intent) {
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    Bundle bundle = new Bundle();
    bundle.putParcelable("BUY_INTENT", pendingIntent);
    bundle.putInt(Utils.RESPONSE_CODE, ResponseCode.OK.getValue());
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
    bundle.putInt(Utils.RESPONSE_CODE, responseCode);
    return bundle;
  }

  public static void setPayflowMethodsList(List<PaymentFlowMethod> paymentFlowMethodsList) {
    if (paymentFlowMethodsList != null) {
      paymentFlowMethods = paymentFlowMethodsList;
    }
  }

  public static List<PaymentFlowMethod> getPayflowMethodsList() {
    if (paymentFlowMethods == null || paymentFlowMethods.isEmpty()) {
      return Collections.emptyList();
    } else {
      return paymentFlowMethods;
    }
  }

  public static String getBillingServicePackageName() {
    if (billingPackageName == null) {
      setBillingServiceInfoToBind();
    }
    return billingPackageName;
  }

  public static String getBillingServiceIabAction() {
    if (billingIabAction == null) {
      setBillingServiceInfoToBind();
    }
    return billingIabAction;
  }

  public static void setBillingServiceInfoToBind() {
    if ( paymentFlowMethods == null && billingPackageName == null) {
      setDefaultBillingServiceInfoToBind();
    } else if (paymentFlowMethods != null && !paymentFlowMethods.isEmpty()) {
      setBillingServiceInfoToBind(paymentFlowMethods.get(0));
    }
  }
  public static void setDefaultBillingServiceInfoToBind() {
    if (isAppAvailableToBind(BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION)) {
      billingPackageName = BuildConfig.APPCOINS_WALLET_PACKAGE_NAME;
      billingIabAction = BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION;
    } else if (isAppAvailableToBind(BuildConfig.GAMESHUB_IAB_BIND_ACTION)) {
      billingPackageName = BuildConfig.GAMESHUB_PACKAGE_NAME;
      billingIabAction = BuildConfig.GAMESHUB_IAB_BIND_ACTION;
    } else {
      billingPackageName = null;
      billingIabAction = null;
    }
  }

  public static void setBillingServiceInfoToBind(PaymentFlowMethod method) {
    if (method instanceof PaymentFlowMethod.Wallet) {
      billingPackageName = BuildConfig.APPCOINS_WALLET_PACKAGE_NAME;
      billingIabAction = BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION;
    } else if (method instanceof PaymentFlowMethod.GamesHub) {
      billingPackageName = BuildConfig.GAMESHUB_PACKAGE_NAME;
      billingIabAction = BuildConfig.GAMESHUB_IAB_BIND_ACTION;
    } else {
      billingPackageName = null;
      billingIabAction = null;
    }
  }

  public static boolean isAppAvailableToBind(String action) {
    Intent intent = new Intent(action);
    List<ResolveInfo> resolveInfoList = context.getPackageManager()
        .queryIntentServices(intent, 0);
    return !resolveInfoList.isEmpty();
  }

  public static int getAppInstalledVersion(String packageName) {
    try {
      PackageInfo packageInfo = context.getPackageManager()
          .getPackageInfo(packageName, 0);
      //VersionCode is deprecated for api 28
      if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return (int) packageInfo.getLongVersionCode();
      } else {
        //noinspection deprecation
        return packageInfo.versionCode;
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return -1;
    }
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
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override public void run() {
        Indicative.launch(context, BuildConfig.INDICATIVE_API_KEY);
      }
    });
    IndicativeAnalytics.INSTANCE.setInstanceId(String.valueOf(getPayAsGuestSessionId()));
    IndicativeAnalytics.INSTANCE.setIndicativeSuperProperties(packageName, BuildConfig.VERSION_CODE,
        getDeviceInfo());
    sdkAnalytics = new SdkAnalytics(AnalyticsManagerProvider.provideAnalyticsManager());
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
