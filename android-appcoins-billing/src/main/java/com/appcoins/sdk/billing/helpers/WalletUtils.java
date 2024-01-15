package com.appcoins.sdk.billing.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.appcoins.billing.sdk.BuildConfig;
import java.util.ArrayList;
import java.util.List;

import static com.appcoins.sdk.billing.helpers.CafeBazaarUtils.getUserCountry;
import static com.appcoins.sdk.billing.helpers.CafeBazaarUtils.userFromIran;

public class WalletUtils {

  private static final int UNINSTALLED_APTOIDE_VERSION_CODE = 0;

  public static Context context;
  private static String billingPackageName;
  private static String iabAction;
  private static String userAgent = null;
  private static Long payAsGuestSessionId;
  private static LifecycleActivityProvider lifecycleActivityProvider;

  public static boolean hasWalletInstalled() {
    if (billingPackageName == null) {
      getPackageToBind();
    }
    return billingPackageName != null;
  }

  private static void getPackageToBind() {
    List<String> intentServicesResponse = new ArrayList<>();
    if ((isAppInstalled(BuildConfig.CAFE_BAZAAR_PACKAGE_NAME, context.getPackageManager())
        || userFromIran(getUserCountry(context)))) {
      iabAction = BuildConfig.CAFE_BAZAAR_IAB_BIND_ACTION;
    } else {
      iabAction = BuildConfig.IAB_BIND_ACTION;
    }
    Intent serviceIntent = new Intent(iabAction);

    List<ResolveInfo> intentServices = context.getPackageManager()
        .queryIntentServices(serviceIntent, 0);

    if (intentServices != null && intentServices.size() > 0) {
      for (ResolveInfo intentService : intentServices) {
        intentServicesResponse.add(intentService.serviceInfo.packageName);
      }
      billingPackageName = chooseServiceToBind(intentServicesResponse, iabAction);
    }
  }

  private static String chooseServiceToBind(List<String> packageNameServices, String action) {
    if (action.equals(BuildConfig.CAFE_BAZAAR_IAB_BIND_ACTION)) {
      if (packageNameServices.contains(BuildConfig.CAFE_BAZAAR_WALLET_PACKAGE_NAME)) {
        return BuildConfig.CAFE_BAZAAR_WALLET_PACKAGE_NAME;
      }
      return null;
    } else {
      String[] packagesOrdered = BuildConfig.SERVICE_BIND_LIST.split(",");
      for (String address : packagesOrdered) {
        if (packageNameServices.contains(address)) {
          return address;
        }
      }
    }
    return null;
  }

  static boolean isAppInstalled(String packageName, PackageManager packageManager) {
    try {
      packageManager.getPackageInfo(packageName, 0);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
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

  static void setPayAsGuestSessionId() {
    payAsGuestSessionId = System.currentTimeMillis();
  }

  public static long getPayAsGuestSessionId() {
    if (payAsGuestSessionId == null) {
      payAsGuestSessionId = System.currentTimeMillis();
    }
    return payAsGuestSessionId;
  }

  public static String getBillingServicePackageName() {
    if (billingPackageName == null) {
      getPackageToBind();
    }
    return billingPackageName;
  }

  public static String getIabAction() {
    if (iabAction == null) {
      if ((isAppInstalled(BuildConfig.CAFE_BAZAAR_PACKAGE_NAME, context.getPackageManager())
          || userFromIran(getUserCountry(context)))) {
        iabAction = BuildConfig.CAFE_BAZAAR_IAB_BIND_ACTION;
      } else {
        iabAction = BuildConfig.IAB_BIND_ACTION;
      }
    }
    return iabAction;
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
