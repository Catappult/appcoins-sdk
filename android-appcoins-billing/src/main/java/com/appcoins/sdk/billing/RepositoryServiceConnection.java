package com.appcoins.sdk.billing;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.PayflowPriorityStream;
import com.appcoins.sdk.billing.managers.BillingLifecycleManager;
import com.appcoins.sdk.billing.payflow.models.PaymentFlowMethod;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logInfo;

public class RepositoryServiceConnection
    implements ServiceConnection, RepositoryConnection, PayflowPriorityStream.Consumer<ArrayList<PaymentFlowMethod>> {
    private final Context context;
    private final ConnectionLifeCycle connectionLifeCycle;
    private AppCoinsBillingStateListener listener;

    public RepositoryServiceConnection(Context context, ConnectionLifeCycle connectionLifeCycle) {
        this.context = context;
        this.connectionLifeCycle = connectionLifeCycle;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        logInfo("Service connected.");
        logDebug("called with: name = [" + name + "], service = [" + service + "]");
        connectionLifeCycle.onConnect(name, service, listener);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        logInfo("Service disconnected.");
        logDebug("called with: name = [" + name + "]");
        connectionLifeCycle.onDisconnect(listener);
    }

    @Override
    public void onBindingDied(ComponentName name) {
        logInfo("Binding died.");
        connectionLifeCycle.onDisconnect(listener);
    }

    @Override
    public void onNullBinding(ComponentName name) {
        logInfo("Binding is null.");
        connectionLifeCycle.onDisconnect(listener);
    }

    @Override
    public void startConnection(final AppCoinsBillingStateListener listener) {
        logInfo("Starting connection to the BillingService.");
        this.listener = listener;
        WalletUtils.INSTANCE.startAnalytics(context.getPackageName());

        BillingLifecycleManager.setupBillingService(context);

        initializeObservableForPayflowPriorityChanges();
    }

    private void initializeObservableForPayflowPriorityChanges() {
        logInfo("Setup collector for PayflowPriorityStream.");
        PayflowPriorityStream.getInstance()
            .collect(this);
    }

    @Override
    public void endConnection() {
        logInfo("Ending connection.");
        BillingLifecycleManager.finishBillingService(context);
        PayflowPriorityStream.getInstance()
            .stopCollecting();
        WalletBinderUtil.finishBillingRepository(context, this);
    }

    @Override
    public void accept(@Nullable ArrayList<PaymentFlowMethod> paymentFlowMethods) {
        logInfo("New result received from PayflowPriorityStream.");
        if (paymentFlowMethods != null) {
            logInfo(String.format("PaymentFlowMethods size: %s", paymentFlowMethods.size()));
            for (PaymentFlowMethod paymentFlowMethod : paymentFlowMethods) {
                logInfo(paymentFlowMethod.toString());
            }
        } else {
            logInfo("PaymentFlowMethods is null.");
        }
        Runnable runnable = () -> WalletBinderUtil.initializeBillingRepository(context, this, paymentFlowMethods);
        new Thread(runnable).start();
    }
}
