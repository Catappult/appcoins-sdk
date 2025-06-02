package com.appcoins.sdk.billing;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.appcoins.communication.requester.MainThreadException;
import com.appcoins.sdk.billing.activities.UpdateDialogActivity;
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.helpers.AnalyticsMappingHelper;
import com.appcoins.sdk.billing.helpers.PayloadHelper;
import com.appcoins.sdk.billing.helpers.QueryProductDetailsParamsMapper;
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    public PurchasesResult queryPurchasesAsync(QueryPurchasesParams queryPurchasesParams) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQueryPurchasesRequestEvent(queryPurchasesParams.getProductType());

        PurchasesResult result = billing.queryPurchases(queryPurchasesParams.getProductType());

        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQueryPurchasesResultEvent(new AnalyticsMappingHelper().mapPurchasesToListOfStrings(result));

        return result;
    }

    @Override
    public void queryPurchasesAsync(QueryPurchasesParams queryPurchasesParams,
        PurchasesResponseListener purchasesResponseListener) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQueryPurchasesRequestEvent(queryPurchasesParams.getProductType());

        billing.queryPurchasesAsync(queryPurchasesParams, purchasesResponseListener);
    }

    @Override
    public void queryProductDetailsAsync(QueryProductDetailsParams queryProductDetailsParams,
        ProductDetailsResponseListener productDetailsResponseListener) {
        QueryProductDetailsParamsMapper queryProductDetailsParamsMapper = new QueryProductDetailsParamsMapper();
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQuerySkuDetailsRequestEvent(
                queryProductDetailsParamsMapper.mapProductDetailsListToProductIdsList(queryProductDetailsParams),
                queryProductDetailsParamsMapper.getProductIdFromQueryProductDetailsParams(queryProductDetailsParams));
        billing.queryProductDetailsAsync(queryProductDetailsParams, productDetailsResponseListener);
    }

    @Override
    public void consumeAsync(ConsumeParams consumeParams, ConsumeResponseListener consumeResponseListener) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendConsumePurchaseRequest(consumeParams.getPurchaseToken());
        billing.consumeAsync(consumeParams.getPurchaseToken(), consumeResponseListener);
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
                ApplicationUtils.handleActivityResult(sdkPaymentResponse.getResultCode(),
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
        ApplicationUtils.handleActivityResult(sdkPaymentResponse.getResultCode(), sdkPaymentResponse.getIntent(),
            purchaseFinishedListener);
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
                ApplicationUtils.handleActivityResult(sdkPaymentResponse.getResultCode(),
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

    /**
     * Deprecated. Use
     * {@link CatapultAppcoinsBilling#queryPurchasesAsync(QueryPurchasesParams, PurchasesResponseListener)} or
     * {@link CatapultAppcoinsBilling#queryPurchasesAsync(QueryPurchasesParams)} instead.
     *
     * @param skuType Type of SKU to be searched.
     */
    @Override
    @Deprecated
    public PurchasesResult queryPurchases(String skuType) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQueryPurchasesRequestEvent(skuType);

        PurchasesResult result = billing.queryPurchases(skuType);

        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQueryPurchasesResultEvent(new AnalyticsMappingHelper().mapPurchasesToListOfStrings(result));

        return result;
    }

    /**
     * Deprecated. Use
     * {@link CatapultAppcoinsBilling#queryProductDetailsAsync(QueryProductDetailsParams, ProductDetailsResponseListener)}
     * instead.
     *
     * @param skuDetailsParams {@link SkuDetailsParams} of the SKUs to be searched.
     * @param onSkuDetailsResponseListener {@link SkuDetailsResponseListener} listener to which the SKU Details will
     * be sent.
     */
    @Override
    @Deprecated
    public void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
        SkuDetailsResponseListener onSkuDetailsResponseListener) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendQuerySkuDetailsRequestEvent(skuDetailsParams.getMoreItemSkus(), skuDetailsParams.getItemType());
        billing.querySkuDetailsAsync(skuDetailsParams, onSkuDetailsResponseListener);
    }

    /**
     * Deprecated. Use
     * {@link CatapultAppcoinsBilling#consumeAsync(ConsumeParams, ConsumeResponseListener)}
     * instead.
     */
    @Override
    @Deprecated
    public void consumeAsync(String token, ConsumeResponseListener consumeResponseListener) {
        SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
            .sendConsumePurchaseRequest(token);
        billing.consumeAsync(token, consumeResponseListener);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface BillingResponseCode {
        /**
         * Requested Feature is not supported by the SDK Version or the Billing Service used.
         */
        int FEATURE_NOT_SUPPORTED = -2;
        /**
         * Success
         */
        int OK = 0;

        /**
         * User pressed back or canceled a dialog
         */
        int USER_CANCELED = 1;

        /**
         * The network connection is down
         */
        int SERVICE_UNAVAILABLE = 2;

        /**
         * This billing API version is not supported for the type requested
         */
        int BILLING_UNAVAILABLE = 3;

        /**
         * Requested SKU is not available for purchase
         */
        int ITEM_UNAVAILABLE = 4;

        /**
         * Invalid arguments provided to the API
         */
        int DEVELOPER_ERROR = 5;

        /**
         * Fatal error during the API action
         */
        int ERROR = 6;

        /**
         * Failure to purchase since item is already owned
         */
        int ITEM_ALREADY_OWNED = 7;

        /**
         * Failure to consume since item is not owned
         */
        int ITEM_NOT_OWNED = 8;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProductType {

        /**
         * One Time Product type.
         */
        @NonNull
        String INAPP = "inapp";

        /**
         * Subscription Product type.
         */
        @NonNull
        String SUBS = "subs";
    }
}
