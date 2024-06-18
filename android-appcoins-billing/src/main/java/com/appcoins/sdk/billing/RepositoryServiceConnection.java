package com.appcoins.sdk.billing;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.PayflowPriorityStream;
import com.appcoins.sdk.billing.managers.BillingLifecycleManager;
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class RepositoryServiceConnection implements ServiceConnection, RepositoryConnection, PayflowPriorityStream.Consumer<ArrayList<PaymentFlowMethod>> {
    private static final String TAG = RepositoryServiceConnection.class.getSimpleName();
    private final Context context;
    private final ConnectionLifeCycle connectionLifeCycle;
    private AppCoinsBillingStateListener listener;

    public RepositoryServiceConnection(Context context, ConnectionLifeCycle connectionLifeCycle) {
        this.context = context;
        this.connectionLifeCycle = connectionLifeCycle;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG,
                "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
        connectionLifeCycle.onConnect(name, service, listener);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
        connectionLifeCycle.onDisconnect(listener);
    }

    @Override
    public void onBindingDied(ComponentName name) {
        connectionLifeCycle.onDisconnect(listener);
    }

    @Override
    public void onNullBinding(ComponentName name) {
        connectionLifeCycle.onDisconnect(listener);
    }

    @Override
    public void startConnection(final AppCoinsBillingStateListener listener) {
        this.listener = listener;
        WalletUtils.startIndicative(context.getPackageName());

        BillingLifecycleManager.setupBillingService(context);

        initializeObservableForPayflowPriorityChanges();
    }

    private void initializeObservableForPayflowPriorityChanges() {
        PayflowPriorityStream.getInstance().collect(this);
    }

    @Override
    public void endConnection() {
        BillingLifecycleManager.finishBillingService(context);
        PayflowPriorityStream.getInstance().stopCollecting();
        WalletBinderUtil.finishBillingRepository(context, this);
    }

    @Override
    public void accept(@Nullable ArrayList<PaymentFlowMethod> paymentFlowMethods) {
        WalletBinderUtil.finishBillingRepository(context, this);
        WalletBinderUtil.initializeBillingRepository(context, this, paymentFlowMethods);
    }
}
