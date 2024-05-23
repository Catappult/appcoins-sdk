package com.appcoins.sdk.billing;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.helpers.AppcoinsBillingStubHelper;
import com.appcoins.sdk.billing.helpers.BindType;
import com.appcoins.sdk.billing.helpers.IBinderWalletNotInstalled;
import com.appcoins.sdk.billing.helpers.WalletUtils;

public class WalletBinderUtil {

  private static BindType bindType = BindType.AIDL;

  private static boolean BillingServiceNotInstalledBehaviour(ServiceConnection connection) {
    bindType = BindType.BILLING_SERVICE_NOT_INSTALLED;
    connection.onServiceConnected(
        new ComponentName("", AppcoinsBillingStubHelper.class.getSimpleName()),
        new IBinderWalletNotInstalled());
    return true;
  }

  @SuppressLint("ObsoleteSdkInt")
  private static boolean bindFailedBehaviour(ServiceConnection connection) {
    if (BuildConfig.URI_COMMUNICATION
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      bindType = BindType.URI_CONNECTION;
      connection.onServiceConnected(
          new ComponentName("", UriCommunicationAppcoinsBilling.class.getSimpleName()),
          new IBinderWalletNotInstalled());
    }
    return true;
  }

  private static boolean BillingServiceInstalledBehaviour(Context context,
      final ServiceConnection connection, Intent serviceIntent, int serviceIntentFlags) {
    if (context.bindService(serviceIntent, connection, serviceIntentFlags)) {
      bindType = BindType.AIDL;
    } else {
      return bindFailedBehaviour(connection);
    }
    return true;
  }

  public static boolean bindService(Context context, Intent serviceIntent,
      ServiceConnection connection, int serviceIntentFlags) {
    if (WalletUtils.hasBillingServiceInstalled()) {
      return BillingServiceInstalledBehaviour(context, connection, serviceIntent,
          serviceIntentFlags);
    } else {
      return BillingServiceNotInstalledBehaviour(connection);
    }
  }

  public static BindType getBindType() {
    return bindType;
  }
}
