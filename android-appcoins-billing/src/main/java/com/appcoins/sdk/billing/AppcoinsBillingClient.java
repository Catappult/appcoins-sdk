package com.appcoins.sdk.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import com.appcoins.sdk.billing.helpers.AppCoinsAndroidBillingRepository;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.appcoins.sdk.billing.usecases.LogGeneralInformation;
import com.appcoins.sdk.core.logger.Logger;
import com.appcoins.sdk.core.security.PurchasesSecurityHelper;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.jetbrains.annotations.NotNull;

import static com.appcoins.sdk.core.logger.Logger.logInfo;

public abstract class AppcoinsBillingClient {

    /**
     * Use this method to build the {@link AppcoinsBillingClient}.
     *
     * @param context {@link Context} of the Application.
     *
     * @return {@link Builder} to build the {@link AppcoinsBillingClient}.
     */
    public static Builder newBuilder(Context context) {
        return new Builder(context);
    }

    /**
     * Call this method to obtain the Purchases associated to the User.
     *
     * @param queryPurchasesParams {@link QueryPurchasesParams} parameters for the querying of the Purchases.
     *
     * @return {@link PurchasesResult}.
     */
    public abstract PurchasesResult queryPurchasesAsync(QueryPurchasesParams queryPurchasesParams);

    /**
     * Call this method to obtain the Purchases associated to the User.
     *
     * @param queryPurchasesParams {@link QueryPurchasesParams} parameters for the querying of the Purchases.
     * @param purchasesResponseListener {@link PurchasesResponseListener} listener to which the Purchases will be sent.
     */
    public abstract void queryPurchasesAsync(QueryPurchasesParams queryPurchasesParams,
        PurchasesResponseListener purchasesResponseListener);

    /**
     * Call this method to obtain the details of the Products available for your application.
     *
     * @param queryProductDetailsParams {@link QueryProductDetailsParams} of the Products to be searched.
     * @param productDetailsResponseListener {@link ProductDetailsResponseListener} listener to which the Product
     * Details will
     * be sent.
     */
    public abstract void queryProductDetailsAsync(QueryProductDetailsParams queryProductDetailsParams,
        ProductDetailsResponseListener productDetailsResponseListener);

    /**
     * Call this method to consume a Purchase.
     * It is important to call this method after a Purchase is made in order to finalize the Payment and consume it.
     * Not calling this method will lead to a refund of the Payment made by the User.
     *
     * @param consumeParams {@link ConsumeParams} of the Purchase to be consumed.
     * @param consumeResponseListener listener to where the status of the Consume will be provided.
     */
    public abstract void consumeAsync(ConsumeParams consumeParams, ConsumeResponseListener consumeResponseListener);

    /**
     * Call this method to launch a billing flow for an SKU.
     * <p>
     * <b>Can't be called in the Main/UI Thread. Use IO Thread when executing this method.</b>
     *
     * @return {@link BillingResult} with the {@link BillingResponseCode} and a Debug message.
     */
    public abstract BillingResult launchBillingFlow(Activity activity, BillingFlowParams billingFlowParams);

    /**
     * Call this method to initialize the Connection to the BillingClient.
     *
     * @param listener of type {@link AppCoinsBillingStateListener} to which the connection state will be provided.
     */
    public abstract void startConnection(AppCoinsBillingStateListener listener);

    /**
     * Method to end any connection to the BillingClient.
     */
    public abstract void endConnection();

    /**
     * Call this method to verify the status of the BillingClient.
     *
     * @return Boolean value if the BillingClient is ready to be interacted with.
     */
    public abstract boolean isReady();

    /**
     * @param requestCode the requestCode received from the onActivityResult method.
     * @param resultCode the resultCode received from the onActivityResult method.
     * @param data the data received from the onActivityResult method.
     *
     * @return Boolean value if the result was processed correctly.
     *
     * @deprecated This method is no longer necessary in the Billing Integration.
     */
    @Deprecated
    public abstract boolean onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Method to obtain the Deeplink to the correct store from which the Application got installed.
     * <p>
     * <b>Can't be called in the Main/UI Thread. Use IO Thread when executing this method.</b>
     *
     * @return {@link ReferralDeeplink}.
     */
    public abstract ReferralDeeplink getReferralDeeplink();

    /**
     * Call this method to verify if an update is available for your application.
     *
     * @return If there is an update available for your application.
     */
    public abstract boolean isAppUpdateAvailable();

    /**
     * Call this method to launch the Store from which your application was installed in your applications page.
     */
    public abstract void launchAppUpdateStore(Context context);

    /**
     * This method will launch a PopUp Dialog when a new version of your application is available and
     * if the User proceeds, will launch the Store from which your application was installed in your applications page.
     */
    public abstract void launchAppUpdateDialog(Context context);

    /**
     * Call this method to verify if a {@link FeatureType} is supported in the current SDK Version and
     * the Billing Service that it is using.
     *
     * @return Integer value of the ResponseCode. ResponseCode.OK if the Feature is Supported.
     */
    public abstract BillingResult isFeatureSupported(FeatureType feature);

    /**
     * This method is deprecated, use {@link #queryPurchasesAsync(QueryPurchasesParams, PurchasesResponseListener)}
     * or {@link #queryPurchasesAsync(QueryPurchasesParams)} instead.
     * Call this method to obtain the Purchases associated to the User.
     *
     * @param skuType {@link ProductType} value of the Type of Purchase to search.
     *
     * @return {@link PurchasesResult}.
     */
    @Deprecated
    public abstract PurchasesResult queryPurchases(String skuType);

    /**
     * This method is deprecated, use
     * {@link #queryProductDetailsAsync(QueryProductDetailsParams, ProductDetailsResponseListener)} instead.
     * <p>
     * Call this method to obtain the details of the SKUs available in your application.
     *
     * @param skuDetailsParams {@link SkuDetailsParams} of the SKUs to be searched.
     * @param onSkuDetailsResponseListener {@link SkuDetailsResponseListener} listener to which the SKU Details will
     * be sent.
     */
    @Deprecated
    public abstract void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
        SkuDetailsResponseListener onSkuDetailsResponseListener);

    /**
     * This method is deprecated, use {@link #consumeAsync(ConsumeParams, ConsumeResponseListener)} instead.
     * <p>
     * Call this method to consume a Purchase.
     * It is important to call this method after a Purchase is made in order to finalize the Payment and consume it.
     * Not calling this method will lead to a refund of the Payment made by the User.
     *
     * @param token token {@link Purchase#getPurchaseToken()} of the Purchase to be consumed.
     * @param consumeResponseListener listener to where the status of the Consume will be provided.
     */
    @Deprecated
    public abstract void consumeAsync(String token, ConsumeResponseListener consumeResponseListener);

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
        @NotNull String INAPP = "inapp";

        /**
         * Subscription Product type.
         */
        @NotNull String SUBS = "subs";
    }

    public static final class Builder {
        private volatile PurchasesUpdatedListener purchasesUpdatedListener;
        private volatile String publicKey;
        private final Context context;

        private Builder(Context context) {
            this.context = context;
        }

        public Builder setListener(PurchasesUpdatedListener listener) {
            this.purchasesUpdatedListener = listener;
            return this;
        }

        public Builder setPublicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public AppcoinsBillingClient build() {
            logInfo("Starting setup of AppcoinsBillingClient.");

            if (this.context == null) {
                throw new IllegalArgumentException("Please provide a valid Context for your application.");
            }

            if (this.purchasesUpdatedListener == null) {
                throw new IllegalArgumentException("Please provide a valid listener for the purchases updates.");
            }

            if (this.publicKey == null) {
                throw new IllegalArgumentException("Please provide a valid public key for the purchases updates.");
            }

            Logger.setupLogger(context);
            LogGeneralInformation.INSTANCE.invoke(context);

            AppCoinsAndroidBillingRepository repository =
                new AppCoinsAndroidBillingRepository(3, context.getPackageName());

            RepositoryServiceConnection connection =
                new RepositoryServiceConnection(context.getApplicationContext(), repository);
            WalletUtils.INSTANCE.setContext(context.getApplicationContext());

            byte[] base64DecodedPublicKey = Base64.decode(publicKey, Base64.DEFAULT);
            PurchasesSecurityHelper.INSTANCE.setBase64DecodedPublicKey(base64DecodedPublicKey);

            return new CatapultAppcoinsBilling(new AppCoinsBilling(repository), connection, purchasesUpdatedListener);
        }
    }
}
