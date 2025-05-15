package com.appcoins.sdk.billing;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import com.appcoins.communication.requester.MainThreadException;
import com.appcoins.sdk.billing.activities.UpdateDialogActivity;
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.helpers.AnalyticsMappingHelper;
import com.appcoins.sdk.billing.helpers.PayloadHelper;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.PendingPurchaseStream;
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences;
import com.appcoins.sdk.billing.usecases.GetReferralDeeplink;
import com.appcoins.sdk.billing.usecases.ingameupdates.IsUpdateAvailable;
import com.appcoins.sdk.billing.usecases.ingameupdates.LaunchAppUpdate;
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils;
import kotlin.Pair;
import org.jetbrains.annotations.Nullable;

import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logError;
import static com.appcoins.sdk.core.logger.Logger.logInfo;

public class CatapultAppcoinsBilling
    implements AppcoinsBillingClient, PendingPurchaseStream.Consumer<Pair<Activity, BuyItemProperties>> {

    private final Billing billing;
    private final RepositoryConnection connection;
    private final PurchasesUpdatedListener purchaseFinishedListener;

    public CatapultAppcoinsBilling(Billing billing, RepositoryConnection connection,
        PurchasesUpdatedListener purchaseFinishedListener) {
        this.billing = billing;
        this.connection = connection;
        this.purchaseFinishedListener = purchaseFinishedListener;
    }

    @Override
    public PurchasesResult queryPurchases(String skuType) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQueryPurchasesRequestEvent(skuType);

        PurchasesResult result = billing.queryPurchases(skuType);

        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQueryPurchasesResultEvent(new AnalyticsMappingHelper().mapPurchasesToListOfStrings(result));

        return result;
    }

    @Override
    public void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
        SkuDetailsResponseListener onSkuDetailsResponseListener) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQuerySkuDetailsRequestEvent(skuDetailsParams.getMoreItemSkus(), skuDetailsParams.getItemType());
        billing.querySkuDetailsAsync(skuDetailsParams, onSkuDetailsResponseListener);
    }

    @Override
    public void consumeAsync(String token, ConsumeResponseListener consumeResponseListener) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendConsumePurchaseRequest(token);
        billing.consumeAsync(token, consumeResponseListener);
    }

    @Override
    public int launchBillingFlow(Activity activity, BillingFlowParams billingFlowParams) {

        int responseCode;

        try {
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendLaunchPurchaseEvent(billingFlowParams.getSku(), billingFlowParams.getSkuType(),
                    billingFlowParams.getDeveloperPayload(), billingFlowParams.getOrderReference(),
                    billingFlowParams.getOrigin(), billingFlowParams.getObfuscatedAccountId(),
                    billingFlowParams.getFreeTrial());

            if (Looper.myLooper() == Looper.getMainLooper()) {
                SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                    .sendLaunchPurchaseMainThreadFailureEvent();
                return handleErrorTypeResponse(ResponseCode.DEVELOPER_ERROR.getValue(),
                    new MainThreadException("launchBillingFlow"));
            }

            String payload = PayloadHelper.buildIntentPayload(billingFlowParams.getOrderReference(),
                billingFlowParams.getDeveloperPayload(), billingFlowParams.getOrigin(),
                billingFlowParams.getObfuscatedAccountId(), billingFlowParams.getFreeTrial());
            AttributionSharedPreferences attributionSharedPreferences = new AttributionSharedPreferences(activity);
            String oemid = attributionSharedPreferences.getOemId();
            String guestWalletId = attributionSharedPreferences.getWalletId();

            logDebug("Launching billing flow with payload: "
                + payload
                + " oemid: "
                + oemid
                + " guestWalletId: "
                + guestWalletId);

            LaunchBillingFlowResult launchBillingFlowResult =
                billing.launchBillingFlow(billingFlowParams, payload, oemid, guestWalletId);

            responseCode = launchBillingFlowResult.getResponseCode();

            if (responseCode != ResponseCode.OK.getValue()) {
                logError("Failed to launch billing flow. ResponseCode: " + responseCode);
                SDKPaymentResponse sdkPaymentResponse = SDKPaymentResponse.Companion.createErrorTypeResponse();
                ApplicationUtils.handleActivityResult(billing, sdkPaymentResponse.getResultCode(),
                    sdkPaymentResponse.getIntent(), purchaseFinishedListener);
                return responseCode;
            }

            Intent buyIntent = launchBillingFlowResult.getBuyIntent();

            PaymentsResultsManager.getInstance()
                .collectPaymentResult(this);

            if (buyIntent != null) {
                activity.startActivity(buyIntent);
            }
        } catch (NullPointerException | ActivityNotFoundException e) {
            return handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e);
        } catch (ServiceConnectionException e) {
            return handleErrorTypeResponse(ResponseCode.SERVICE_UNAVAILABLE.getValue(), e);
        }
        return ResponseCode.OK.getValue();
    }

    private int handleErrorTypeResponse(int value, Exception e) {
        logError("Failed to launch billing flow.", e);
        SDKPaymentResponse sdkPaymentResponse = SDKPaymentResponse.Companion.createErrorTypeResponse();
        ApplicationUtils.handleActivityResult(billing, sdkPaymentResponse.getResultCode(),
            sdkPaymentResponse.getIntent(), purchaseFinishedListener);
        return value;
    }

    @Override
    public void startConnection(final AppCoinsBillingStateListener listener) {
        logInfo("Request to start connection of SDK.");
        if (!isReady()) {
            logInfo("Starting connection of SDK.");
            PendingPurchaseStream.getInstance()
                .collect(this);
            connection.startConnection(listener);
        }
    }

    @Override
    public void endConnection() {
        logInfo("Request to end connection of SDK.");
        if (isReady()) {
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendFinishConnectionEvent();
            logInfo("Ending connection of SDK.");
            PendingPurchaseStream.getInstance()
                .stopCollecting();
            connection.endConnection();
        }
    }

    @Override
    public boolean isReady() {
        return billing.isReady();
    }

    @Override
    public boolean isAppUpdateAvailable() {
        logInfo("Request to verify AppUpdateAvailable.");
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendAppUpdateAvailableRequest();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            logInfo("Request from MainThread. Cancelling.");
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendAppUpdateAvailableMainThreadFailure();
            return false;
        } else {
            boolean result = IsUpdateAvailable.INSTANCE.invoke(WalletUtils.INSTANCE.getContext());
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendAppUpdateAvailableResult(result);
            return result;
        }
    }

    @Override
    public void launchAppUpdateStore(Context context) {
        logInfo("Request to launch App Update Store.");
        Runnable runnable = () -> {
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendLaunchAppUpdateStoreRequestEvent();
            LaunchAppUpdate.INSTANCE.invoke(context);
        };
        new Thread(runnable).start();
    }

    @Override
    public void launchAppUpdateDialog(Context context) {
        logInfo("Request to launch App Update Dialog.");
        Runnable runnable = () -> {
            Intent updateDialogActivityIntent = new Intent(context.getApplicationContext(), UpdateDialogActivity.class);
            updateDialogActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext()
                .startActivity(updateDialogActivityIntent);
        };
        new Thread(runnable).start();
    }

    @Override
    public ReferralDeeplink getReferralDeeplink() {
        logInfo("Request to get Referral Deeplink.");
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendGetReferralDeeplinkRequestEvent();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            logInfo("Request from MainThread. Cancelling.");
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendGetReferralDeeplinkMainThreadFailureEvent();
            return new ReferralDeeplink(ResponseCode.DEVELOPER_ERROR, null, null);
        } else {
            return GetReferralDeeplink.INSTANCE.invoke();
        }
    }

    @Override
    public int isFeatureSupported(FeatureType feature) {
        logInfo("Request to verify if Feature is supported.");
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendIsFeatureSupportedRequestEvent(feature.name());
        int result = billing.isFeatureSupported(feature);
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendIsFeatureSupportedResultEvent(result);
        return result;
    }

    @Deprecated
    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return true;
    }

    public Billing getBilling() {
        return billing;
    }

    public PurchasesUpdatedListener getPurchaseFinishedListener() {
        return purchaseFinishedListener;
    }

    @Override
    public void accept(@Nullable Pair<Activity, BuyItemProperties> value) {
        Runnable runnable = () -> {
            Looper.prepare();
            resumeBillingFlow(value.component1(), value.component2()
                .toBillingFlowParams());
            Looper.loop();
        };
        new Thread(runnable).start();
    }

    private void resumeBillingFlow(Activity activity, BillingFlowParams billingFlowParams) {
        logInfo("Resuming Billing Flow after Wallet Installation.");
        int responseCode;
        try {
            String payload = PayloadHelper.buildIntentPayload(billingFlowParams.getOrderReference(),
                billingFlowParams.getDeveloperPayload(), billingFlowParams.getOrigin(),
                billingFlowParams.getObfuscatedAccountId(), billingFlowParams.getFreeTrial());
            AttributionSharedPreferences attributionSharedPreferences = new AttributionSharedPreferences(activity);
            String oemid = attributionSharedPreferences.getOemId();
            String guestWalletId = attributionSharedPreferences.getWalletId();

            logDebug("Launching billing flow with payload: "
                + payload
                + " oemid: "
                + oemid
                + " guestWalletId: "
                + guestWalletId);

            LaunchBillingFlowResult launchBillingFlowResult =
                billing.launchBillingFlow(billingFlowParams, payload, oemid, guestWalletId);

            responseCode = launchBillingFlowResult.getResponseCode();

            if (responseCode != ResponseCode.OK.getValue()) {
                logError("Failed to launch billing flow. ResponseCode: " + responseCode);
                SDKPaymentResponse sdkPaymentResponse = SDKPaymentResponse.Companion.createErrorTypeResponse();
                ApplicationUtils.handleActivityResult(billing, sdkPaymentResponse.getResultCode(),
                    sdkPaymentResponse.getIntent(), purchaseFinishedListener);
                return;
            }

            Intent buyIntent = launchBillingFlowResult.getBuyIntent();

            PaymentsResultsManager.getInstance()
                .collectPaymentResult(this);
            if (buyIntent != null) {
                activity.startActivity(buyIntent);
            }
        } catch (NullPointerException | ActivityNotFoundException e) {
            handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e);
        } catch (ServiceConnectionException e) {
            handleErrorTypeResponse(ResponseCode.SERVICE_UNAVAILABLE.getValue(), e);
        }
    }
}
