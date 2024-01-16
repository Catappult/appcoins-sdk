package com.appcoins.sdk.billing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.payflow.PayflowManager;

public class RepositoryServiceConnection implements ServiceConnection, RepositoryConnection {
  private static final String TAG = RepositoryServiceConnection.class.getSimpleName();
  private final Context context;
  private final ConnectionLifeCycle connectionLifeCycle;
  private AppCoinsBillingStateListener listener;
  private boolean hasBillingServiceInstalled;

  public RepositoryServiceConnection(Context context, ConnectionLifeCycle connectionLifeCycle) {
    this.context = context;
    this.connectionLifeCycle = connectionLifeCycle;
  }

  @Override public void onServiceConnected(ComponentName name, IBinder service) {
    Log.d(TAG,
        "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
    connectionLifeCycle.onConnect(name, service, listener);
  }

  @Override public void onServiceDisconnected(ComponentName name) {
    Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
    connectionLifeCycle.onDisconnect(listener);
  }

  @Override public void onBindingDied(ComponentName name) {
    connectionLifeCycle.onDisconnect(listener);
  }

  @Override public void onNullBinding(ComponentName name) {
    connectionLifeCycle.onDisconnect(listener);
  }

  @Override public void startConnection(final AppCoinsBillingStateListener listener) {
    this.listener = listener;
    WalletUtils.startIndicative(context.getPackageName());
    WalletUtils.getSdkAnalytics().sendStartConnetionEvent();

    new PayflowManager(context.getPackageName()).getPayflowPriority();
    hasBillingServiceInstalled = WalletUtils.hasBillingServiceInstalled();

    String packageName = WalletUtils.getBillingServicePackageName();
    String iabAction = WalletUtils.getBillingServiceIabAction();
    Intent serviceIntent = new Intent(iabAction);
    serviceIntent.setPackage(packageName);
    WalletBinderUtil.bindService(context, serviceIntent, this, Context.BIND_AUTO_CREATE);
  }

  @Override public void endConnection() {
    if (hasBillingServiceInstalled) {
      context.unbindService(this);
    }
    connectionLifeCycle.onDisconnect(listener);
  }
}
